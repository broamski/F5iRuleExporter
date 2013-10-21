package com.nuszkowski;

import java.io.*;
import java.rmi.RemoteException;
import java.util.Properties;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.log4j.*;

public class FetchiRules {
	static Logger log = Logger.getLogger(FetchiRules.class);
	private static int exit_code = 0;

	private static void usage(Options options) {
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp("Here is how you use this, ya goof:", options);
	}

	public static String propChecker(String input_string, String identifier) {
		if (input_string == null || input_string.isEmpty()) {
			log.error("Error: " + identifier
					+ " missing or empty in properties file.");
			System.exit(-1);
			return "BAD";
		} else {
			return input_string;
		}
	}

	public static void main(String[] args) throws ParseException,
			FileNotFoundException, IOException {
		Options opt = new Options();
		Option op = new Option("config", true,
				"Full path to config file IE: /Users/willysmits/config.properties");
		op.setRequired(true);
		opt.addOption(op);

		CommandLineParser parser = new BasicParser();
		CommandLine cmd;

		try {
			cmd = parser.parse(opt, args);

			Properties prop = new Properties();
			prop.load(new FileInputStream(cmd.getOptionValue("config")));

			String username = propChecker(prop.getProperty("username"),
					"username");
			String password = propChecker(prop.getProperty("password"),
					"password");
			String auth_server = propChecker(prop.getProperty("authserver"),
					"authserver");
			String ltm_list_string = propChecker(prop.getProperty("ltm.list"),
					"ltm.list");
			String gtm_list_string = propChecker(prop.getProperty("gtm.list"),
					"gtm.list");
			String export_path = propChecker(prop.getProperty("export.path"),
					"export.path");

			String[] ltm_list = new String[ltm_list_string.split(",").length];
			ltm_list = ltm_list_string.split(",");

			String[] gtm_list = new String[gtm_list_string.split(",").length];
			gtm_list = gtm_list_string.split(",");

			log.info("iRule Export Started");
			if (auth_test(auth_server, username, password)) {
				log.info("Successfully authenticated to device " + auth_server);
				LTMiRuleExport(username, password, export_path, ltm_list);
				GTMiRuleExport(username, password, export_path, gtm_list);
			} else {
				log.warn("Authentication with device " + auth_server
						+ " was unsuccessful.");
				exit_code = -1;
			}
			log.info("iRule Export Complete");
			System.exit(exit_code);

		} catch (ParseException pe) {
			usage(opt);
		}
	}

	public static boolean auth_test(String device_name, String user_name,
			String user_password) {
		boolean is_authenticated = true;
		try {
			iControl.Interfaces f_five = new iControl.Interfaces();
			f_five.initialize(device_name, user_name, user_password);
			f_five.getSystemSystemInfo().get_version();
			f_five = null;
		} catch (Exception ex) {
			is_authenticated = false;
			return is_authenticated;
		}
		return is_authenticated;
	}
	
	public static String getMajorVersion(iControl.Interfaces f5_in) throws RemoteException, Exception {
		if (f5_in.getSystemSystemInfo().get_version().contains("11")){
			return "11";
		}
		else if (f5_in.getSystemSystemInfo().get_version().contains("10")){
			return "10";
		}
		else {
			return "NaN";
		}
	}

	public static void LTMiRuleExport(String username, String password,
			String export_path, String[] ltm_list) {
		try {
			File root_dir = new File(export_path);
			if (!root_dir.exists()) {
				log.warn(export_path.toString()
						+ " didn't exist, so it was created.");
				root_dir.mkdir();
			}
		} catch (Exception ex) {
			log.error("Something went wrong.", ex);
			exit_code = -1;
		}

		for (String ltm : ltm_list) {
			try {
				iControl.Interfaces f5 = new iControl.Interfaces();
				f5.initialize(ltm, username, password);
				
				String full_export_path;
				String ltm_major_version = getMajorVersion(f5).toString();
				if (ltm_major_version.equals("11"))
				{
					full_export_path = export_path + "/" + ltm + "/Common";
				} else {
					full_export_path = export_path + "/" + ltm;
				}

				File device_dir = new File(full_export_path);
				
				if (!device_dir.exists()) {
					log.info(full_export_path
							+ " didn't exist, so it was created.");
					device_dir.mkdirs();
				}

				log.info("Exporting iRules from: " + ltm);
				for (iControl.LocalLBRuleRuleDefinition rule : f5
						.getLocalLBRule().query_all_rules()) {
					if (!rule.getRule_name().contains("_sys")) {
						String raw_rule_name = rule.getRule_name(), rule_name;
						if (ltm_major_version.equals("11"))
						{
							rule_name = raw_rule_name.split("/")[2];
						} else {
							rule_name = raw_rule_name;
						}
						FileWriter fstream;
						fstream = new FileWriter(full_export_path
								+ "/" + rule_name + ".txt");
						BufferedWriter out = new BufferedWriter(fstream);
						log.info("Exporting iRule: " + rule_name + " from " + ltm);
						out.write(rule.getRule_definition());
						out.close();
						fstream.close();
					}
				}
			} catch (Exception e) {
				log.error("Something went wrong when enumerating the rules on " + ltm, e);
				exit_code = -1;
			}
		}
	}

	public static void GTMiRuleExport(String username, String password,
			String export_path, String[] gtm_list) {
		try {
			File root_dir = new File(export_path);
			if (!root_dir.exists()) {
				log.warn(export_path.toString()
						+ " didn't exist, so it was created.");
				root_dir.mkdir();
			}
		} catch (Exception ex) {
			log.error("Something went wrong.", ex);
			exit_code = -1;
		}

		for (String gtm : gtm_list) {
			try {				
				iControl.Interfaces f5 = new iControl.Interfaces();
				f5.initialize(gtm, username, password);
				
				String full_export_path;
				String gtm_major_version = getMajorVersion(f5).toString();
				if (gtm_major_version.equals("11"))
				{
					full_export_path = export_path + "/" + gtm + "/Common";
				} else {
					full_export_path = export_path + "/" + gtm;
				}

				File device_dir = new File(full_export_path);
				
				if (!device_dir.exists()) {
					log.info(full_export_path
							+ " didn't exist, so it was created.");
					device_dir.mkdirs();
				}

				FileWriter fstream;

				log.info("Exporting iRules from " + gtm);
				for (iControl.GlobalLBRuleRuleDefinition rule : f5
						.getGlobalLBRule().query_all_rules()) {
					if (!rule.getRule_name().contains("_sys")) {
						String raw_rule_name = rule.getRule_name(), rule_name;
						if (gtm_major_version.equals("11"))
						{
							rule_name = raw_rule_name.split("/")[2];
						} else {
							rule_name = raw_rule_name;
						}
						
						fstream = new FileWriter(full_export_path
								+ "/" + rule_name + ".txt");
						BufferedWriter out = new BufferedWriter(fstream);
						log.info("Exporting iRule: " + rule_name + " from " + gtm);
						out.write(rule.getRule_definition());
						out.close();
						fstream.close();
					}
				}
			} catch (Exception e) {
				log.error("Something went wrong when enumerating the rules on " + gtm, e);
				exit_code = -1;
			}
		}
	}
}
