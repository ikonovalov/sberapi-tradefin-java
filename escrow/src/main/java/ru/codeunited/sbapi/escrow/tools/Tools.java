package ru.codeunited.sbapi.escrow.tools;

import org.apache.commons.cli.*;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.*;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.charset.UnsupportedCharsetException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import static ru.codeunited.sbapi.escrow.tools.Console.*;

public class Tools {

    public static final String ESCROW_V1_XSD = "escrow-v1.xsd";
    public static final String ESCROW_V2_XSD = "escrow-v2.xsd";
    public static final String ESCROW_V3_XSD = "escrow-v3.xsd";

    public static void main(String[] args) throws ParseException, IOException, SAXException {
        // create Options object
        Options options = new Options();

        /* CMS mode options */
        Option modeOpt = Option.builder().desc("Operation mode")
                .argName("cms, uuid").longOpt("mode").hasArg(true).optionalArg(false)
                .build();

        Option fileOpt = Option.builder().desc("Path to file")
                .hasArg().numberOfArgs(1)
                .argName("path to cms").longOpt("file")
                .build();

        Option schemaVersionOpt = Option.builder().desc("Schema version (default = 2)")
                .hasArg().numberOfArgs(1).optionalArg(true)
                .argName("1,2,3").longOpt("schema")
                .build();

        Option charsetOpt = Option.builder().desc("Character encoding for XML")
                .hasArg().numberOfArgs(1).optionalArg(true)
                .argName("utf-8").longOpt("charset")
                .build();

        /* UUID mode options */
        Option idOpt = Option.builder().desc("Id 1234566")
                .hasArg().numberOfArgs(1)
                .argName("id").longOpt("id")
                .build();

        Option uuidOpt = Option.builder().desc("UUID 00-0012-d595-0000-00000012d595")
                .hasArg().numberOfArgs(1)
                .argName("uuid").longOpt("uuid")
                .build();


        options /* All available options */
                .addOption(modeOpt)
                .addOption(fileOpt)
                .addOption(schemaVersionOpt)
                .addOption(charsetOpt)
                .addOption(idOpt)
                .addOption(uuidOpt);

        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(options, args);

        if (isModeOptionNotPassed(modeOpt, cmd)) {
            Options tmpOpts = new Options();
            HelpFormatter formatter = new HelpFormatter();
            formatter.setWidth(200);
            formatter.setLeftPadding(2);
            formatter.setOptionComparator((o1, o2) -> 0);

            formatter.setSyntaxPrefix("Usage: ");
            formatter.printHelp("java -jar escrow-tools.jar [OPTIONS]", tmpOpts, false);

            formatter.setSyntaxPrefix(" Mode options: ");
            tmpOpts.addOption(modeOpt);
            formatter.printHelp(" ", tmpOpts);

            formatter.setSyntaxPrefix(" CMS mode options:");
            tmpOpts = new Options();
            tmpOpts.addOption(fileOpt).addOption(schemaVersionOpt).addOption(charsetOpt);
            formatter.printHelp(" ", tmpOpts);

            formatter.setSyntaxPrefix(" UUID mode options:");
            tmpOpts = new Options();
            tmpOpts.addOption(idOpt).addOption(uuidOpt);
            formatter.printHelp(" ", tmpOpts);


        } else {
            String modeOptionValue = cmd.getOptionValue(modeOpt.getLongOpt()).toLowerCase();
            switch (modeOptionValue) {
                case "cms":
                    info("CMS mode enabled");
                    String path = cmd.getOptionValue(fileOpt.getLongOpt());
                    if (path == null || path.trim().length() == 0) {
                        fail("Option --" + fileOpt.getLongOpt() + " not passed");
                    }

                    String schemaVersionValue = determinateXsdSchamaName(schemaVersionOpt, cmd);
                    byte[] fileContent = readFileOrFail(path);
                    byte[] decoded = decodeBase64OrFail(fileContent);
                    String xml = readXmlOrFail(charsetOpt, cmd, decoded);
                    validateXmlOrFail(xml, schemaVersionValue);
                    info("-----------------------------------------------------------------------");
                    info(xml);
                    break;

                case "uuid":
                    info("UUID mode enabled");
                    Optional.ofNullable(cmd.getOptionValue(idOpt.getLongOpt())).ifPresent(
                            strId -> info("ID=" + strId + " => UUID=" +
                                    new UUID(Long.parseLong(strId), Long.parseLong(strId))
                            )
                    );

                    Optional.ofNullable(cmd.getOptionValue(uuidOpt.getLongOpt())).ifPresent(
                            strUuid -> info("UUID=" + strUuid + " => ID=" +
                                    UUID.fromString(strUuid).getLeastSignificantBits()
                            )
                    );
                    break;
                default:
                    error("Unknown mode");
            }
        }
    }

    private static String determinateXsdSchamaName(Option schemaVersionOpt, CommandLine cmd) {
        String schemaVersionValue;
        if (cmd.hasOption(schemaVersionOpt.getLongOpt())) {
            switch (cmd.getOptionValue(schemaVersionOpt.getLongOpt())) {
                case "1": schemaVersionValue = ESCROW_V1_XSD; break;
                //case "2": schemaVersionValue = ESCROW_V2_XSD; break;
                case "3": schemaVersionValue = ESCROW_V3_XSD; break;
                default: schemaVersionValue = ESCROW_V2_XSD;

            }
        } else {
            schemaVersionValue = ESCROW_V2_XSD;
        }
        return schemaVersionValue;
    }

    private static byte[] readFileOrFail(String path) {
        byte[] fileContent = new byte[0];
        try {
            fileContent = Files.readAllBytes(Paths.get(Objects.requireNonNull(path)));
            ok("File access");
        } catch (Exception e) {
            fail(e.getMessage());
        }
        return fileContent;
    }

    private static byte[] decodeBase64OrFail(byte[] fileContent) {
        byte[] decoded = null;
        try {
            decoded = Base64.getDecoder().decode(fileContent);
            ok("Base64 encoding");
        } catch (Exception e) {
            fail("Base64");
        }
        return decoded;
    }

    private static String readXmlOrFail(Option charsetOpt, CommandLine cmd, byte[] decoded) {
        String xml = "";
        try {
            Charset charset = getCharsetOrFail(charsetOpt, cmd);
            xml = Pkcs7Extractor.readContent(decoded, charset);
            ok("CMS content");
        } catch (Exception e) {
            fail("CMS invalid: " + e.getMessage());
        }
        return xml;
    }

    private static Charset getCharsetOrFail(Option charsetOpt, CommandLine cmd) {
        Charset charset = StandardCharsets.UTF_8;
        if (cmd.hasOption(charsetOpt.getLongOpt())) {
            try {
                charset = Charset.forName(cmd.getOptionValue(charsetOpt.getLongOpt()));
            } catch (UnsupportedCharsetException unsupportedEncodingException) {
                fail("Unsupported charset " + unsupportedEncodingException.getMessage());
            }
        }
        return charset;
    }

    private static void validateXmlOrFail(String xml, String xsdName) {
        Source xmlFile = new StreamSource(new StringReader(xml));
        SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        try {
            URL xsd = Thread.currentThread().getContextClassLoader().getResource(xsdName);
            Schema schema = schemaFactory.newSchema(xsd);
            Validator validator = schema.newValidator();
            validator.validate(xmlFile);
            ok("XML validation [" + xsdName + "]");
        } catch (SAXException | IOException e) {
            String errMsg = e.getMessage();

            // IF some problems with prolog
            if (errMsg.contains("prolog")) {
                // print prolog before '<'
                error("Prolog detected! Scan prolog...");
                char c;
                int idx = 0;
                while((c = xml.charAt(idx++)) != '<') {
                    error("[" + idx + "] \\u" + Integer.toHexString(c | 0x10000).substring(1).toUpperCase() );
                }
            }
            fail("XML validation [" + xsdName + "]: " + errMsg);
        }
    }

    private static boolean isModeOptionNotPassed(Option modeOpt, CommandLine cmd) {
        return !cmd.hasOption(modeOpt.getLongOpt());
    }
}
