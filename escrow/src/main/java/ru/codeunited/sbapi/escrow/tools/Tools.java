package ru.codeunited.sbapi.escrow.tools;

import org.apache.commons.cli.*;
import org.bouncycastle.cms.CMSException;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.IOException;
import java.io.StringReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;

public class Tools {
    public static void main(String[] args) throws ParseException, URISyntaxException, IOException, CMSException, SAXException {
        // create Options object
        Options options = new Options();

        /* CMS mode options */
        Option modeOpt = Option.builder().desc("Work mode")
                .argName("cms,uuid,crt").longOpt("mode").hasArg(true).optionalArg(false)
                .build();

        Option fileOpt = Option.builder().desc("Path to file")
                .hasArg().numberOfArgs(1)
                .argName("path").longOpt("file")
                .build();

        /* UUID mode options */
        Option idOpt = Option.builder().desc("Id 1234566")
                .hasArg().numberOfArgs(1)
                .argName("id").longOpt("id")
                .build();

        Option uuidOpt = Option.builder().desc("UUID")
                .hasArg().numberOfArgs(1)
                .argName("uuid").longOpt("uuid")
                .build();

        /* CRT mode options */
        Option serialOpt = Option.builder().desc("Serial number")
                .hasArg().numberOfArgs(1)
                .argName("serial_number").longOpt("serial")
                .build();

        options /* All available options */
                .addOption(modeOpt)
                .addOption(fileOpt)
                .addOption(idOpt)
                .addOption(uuidOpt)
                .addOption(serialOpt);

        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(options, args);

        if (isModeOptionPassed(modeOpt, cmd)) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("escrow-tools", options, true);
        } else {
            String modeOptionValue = cmd.getOptionValue(modeOpt.getLongOpt()).toLowerCase();
            switch (modeOptionValue) {
                case "cms":
                    System.out.println("CMS mode enabled");
                    String path = cmd.getOptionValue(fileOpt.getLongOpt());
                    if (path == null || path.trim().length() == 0) {
                        System.out.println("Option --" + fileOpt.getLongOpt() + " not passed");
                        System.exit(0);
                    }
                    byte[] fileContent = Files.readAllBytes(Paths.get(path));
                    ok("File access");

                    byte[] decoded = null;
                    try {
                        decoded = Base64.getDecoder().decode(fileContent);
                        ok("Base64 encoding");
                    } catch (Exception e) {
                        fail("Base64");
                    }

                    String xml = "";
                    try {
                        xml = Pkcs7Extractor.readContent(decoded);
                        ok("CMS content");
                    } catch (Exception e) {
                        fail("CMS invalid: " + e.getMessage());
                    }

                    if (validateXML(xml)) {
                        ok("XML validation");
                    }

                    info("XML:\n" + xml);
                    break;
                case "uuid":
                    System.out.println("UUID mode enabled");
                    Optional.ofNullable(cmd.getOptionValue(idOpt.getLongOpt())).ifPresent(
                            strId -> System.out.println("ID=" + strId + " => UUID=" +
                                    new UUID(Long.parseLong(strId), Long.parseLong(strId))
                            )
                    );

                    Optional.ofNullable(cmd.getOptionValue(uuidOpt.getLongOpt())).ifPresent(
                            strUuid -> System.out.println("UUID=" + strUuid + " => ID=" +
                                    UUID.fromString(strUuid).getLeastSignificantBits()
                            )
                    );
                    break;
                default:
                    error("Unknown mode");
            }
        }
    }

    private static boolean validateXML(String xml) throws SAXException, IOException {
        Source xmlFile = new StreamSource(new StringReader(xml));
        SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        try {
            URL xsd = Thread.currentThread().getContextClassLoader().getResource("escrow-v2.xsd");
            Schema schema = schemaFactory.newSchema(xsd);
            Validator validator = schema.newValidator();
            validator.validate(xmlFile);
            return true;
        } catch (SAXException | IOException e) {
            String errMsg = e.getMessage();
            fail("XML validation: " + errMsg);

            // IF some problems with prolog
            if (errMsg.contains("prolog")) {
                // print prolog before '<'
                System.err.println("Prolog detected! Scan prolog...");
                char c = 0;
                int idx = 0;
                while((c = xml.charAt(idx++)) != '<') {
                    System.err.println("[" + idx + "] \\u" + Integer.toHexString(c | 0x10000).substring(1).toUpperCase() );
                }
            }
            return false;
        }
    }

    private static boolean isModeOptionPassed(Option modeOpt, CommandLine cmd) {
        return !cmd.hasOption(modeOpt.getLongOpt());
    }

    private static void error(String msg) {
        System.err.println(msg);
    }

    private static void info(String msg) {
        System.out.println(msg);
    }

    private static void ok(String msg) {
        System.out.println("[OK]\t" + msg);
    }

    private static void fail(String msg) {
        System.err.println("[FAIL]\t" + msg);
        System.exit(1);
    }
}
