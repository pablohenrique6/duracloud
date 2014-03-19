package org.duracloud.auditor.tools;

import java.util.ArrayList;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.duracloud.audit.dynamodb.AuditLogItem;
import org.duracloud.audit.dynamodb.DatabaseUtil;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.model.AttributeDefinition;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.CreateTableResult;
import com.amazonaws.services.dynamodbv2.model.DeleteTableRequest;
import com.amazonaws.services.dynamodbv2.model.DeleteTableResult;
import com.amazonaws.services.dynamodbv2.model.GlobalSecondaryIndex;
import com.amazonaws.services.dynamodbv2.model.KeySchemaElement;
import com.amazonaws.services.dynamodbv2.model.KeyType;
import com.amazonaws.services.dynamodbv2.model.LocalSecondaryIndex;
import com.amazonaws.services.dynamodbv2.model.Projection;
import com.amazonaws.services.dynamodbv2.model.ProjectionType;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;

public class Driver {
    private static Options options;
    private static AmazonDynamoDBClient client;

    /**
     * @param args
     */
    public static void main(String[] args) {
        try {
            options = createOptions();
            CommandLineParser parser = new PosixParser();
            CommandLine cl = parser.parse(options, args);

            client =
                new AmazonDynamoDBClient(new BasicAWSCredentials(cl.getOptionValue("u"),
                                                                 cl.getOptionValue("p")));

            client.setRegion(Region.getRegion(Regions.DEFAULT_REGION));

            if(cl.hasOption("e")){
                String endpoint = cl.getOptionValue("e");
                if(endpoint == null){
                    endpoint = DatabaseUtil.DEFAULT_LOCAL_ENDPOINT;
                }
                client.setEndpoint(endpoint);
            }
            
            if (cl.hasOption("c")) {
                DatabaseUtil.create(client);
            } else if (cl.hasOption("d")) {
                DatabaseUtil.drop(client);
            } else {
                usage();
            }
        } catch (ParseException e) {
            usage();
        }

    }



    


    private static void usage() {
        HelpFormatter help = new HelpFormatter();
        help.printHelp("auditlogtool [options]", options);
        System.exit(1);
    }

    private static Options createOptions() {
        Options options = new Options();
        Option username = new Option("u", true, "aws username");
        username.setRequired(true);
        options.addOption(username);

        Option password = new Option("p", true, "aws password");
        password.setRequired(true);
        options.addOption(password);

        Option createTable =
            new Option("c", "create-table", false, "Create the table");
        createTable.setRequired(false);
        options.addOption(createTable);

        Option dropTable =
            new Option("d", "drop-table", false, "Drop the table");
        dropTable.setRequired(false);
        options.addOption(dropTable);

        Option endPoint =
            new Option("e",
                       "endpoint",
                       true,
                       "Specifies a local end point (Dynamo DB Local: default value = \""
                           + DatabaseUtil.DEFAULT_LOCAL_ENDPOINT + "\")");
        endPoint.setRequired(false);
        endPoint.setOptionalArg(true);
        options.addOption(endPoint);

        return options;
    }

}