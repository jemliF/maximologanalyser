package com.smartech.loganalyser.src.utils;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;


public class LogFileParser {
	private File file;

	public File getFile() {
		return file;
	}

	public void setFile(File file) {
		this.file = file;
	}
	
	public LogFileParser() {
	}

	public LogFileParser(File file) {
		this.file = file;
	}

	public Vector<ObservableList<ObservableList<Object>>> parseSystemOutString(String logEntry){
		
		ObservableList<ObservableList<Object>> parsedLogQueries = FXCollections.observableArrayList();
		ObservableList<ObservableList<Object>> parsedLogExceptions = FXCollections.observableArrayList();
		Vector<ObservableList<ObservableList<Object>>> parsedLogString = new Vector<ObservableList<ObservableList<Object>>>();
		
		parsedLogString.add(parsedLogQueries);
		parsedLogString.add( parsedLogExceptions);
		
		boolean isQueryEntry = false;
		boolean isExceptionEntry = false;
		
		Pattern motifSystemOut = Pattern
				.compile("\\d?\\d/\\d?\\d/\\d?\\d \\d?\\d:\\d?\\d:\\d?\\d:\\d?\\d?\\d");
		Matcher matcherDate = motifSystemOut.matcher(logEntry);
		matcherDate.find();

		// date parsing
		String mySQLDate = null;
		SimpleDateFormat format = new SimpleDateFormat(
				"MM/dd/yy HH:mm:ss");
		Date date = null;
		try {
			date = format.parse(logEntry.substring(matcherDate.start(),
					matcherDate.end()));
			SimpleDateFormat sdf = new SimpleDateFormat(
					"yyyy-MM-dd HH:mm:ss");
			mySQLDate = sdf.format(date);
		} catch (ParseException e) {
			e.printStackTrace();
		} catch (IllegalStateException e) {
			//e.printStackTrace();
			date = new Date();
		}
		String level = "";
		// level parsing
		if (logEntry.contains("ServletWrappe")
				|| logEntry.contains("WebContainer")
				|| logEntry.contains("UserManagerMB")
				|| logEntry.contains("ThreadMonitor")
				|| logEntry.contains("filter")
				|| logEntry.contains("FfdcProvider")
				|| logEntry.contains("FFDCJanitor")
				|| logEntry.contains("SibMessage")) {//
			level = "-";
		} else {
			motifSystemOut = Pattern
					.compile("(?<=\\[)(ERROR|INFO|WARN|DEBUG)(?=\\])");
			Matcher matcherLevel = motifSystemOut.matcher(logEntry);
			if(matcherLevel.find()){
				try {
					level = logEntry.substring(matcherLevel.start(),
							matcherLevel.end());
				} catch (IllegalStateException e) {
					e.printStackTrace();
					level = "-";
				}
			}
		}
		// direction parsing  I | O | E | W | A 
		motifSystemOut = Pattern.compile(" I | O | E | W | A ");
		Matcher matcherDirection = motifSystemOut.matcher(logEntry);
		String direction = "";
		if(matcherDirection.find()){
			try {
				direction = logEntry.substring(matcherDirection.start(),
						matcherDirection.end());
			} catch (IllegalStateException e) {
				e.printStackTrace();
				direction = "-";
			}
		}
		// message parsing \[CID-CRON.*\]|\[\]
		String msg = "";
		if (logEntry.contains("ServletWrappe")
				|| logEntry.contains("WebContainer")
				|| logEntry.contains("UserManagerMB")
				|| logEntry.contains("ThreadMonitor")
				|| logEntry.contains("filter")
				|| logEntry.contains("FfdcProvider")
				|| logEntry.contains("FFDCJanitor")
				|| logEntry.contains("SibMessage")
				|| logEntry.contains("AdminHelper")
				|| logEntry.contains("TCPChannel")
				|| logEntry.contains("JMSProxyRunti")
				|| logEntry.contains("SchedulerServ")
				|| logEntry.contains("SharedEJBRunt")
				|| logEntry.contains("ActivationSpe")
				|| logEntry.contains("distSecurityC")
				|| logEntry.contains("CGBridgeServi")
				|| logEntry.contains("DragDropDeplo")
				|| logEntry.contains("DCSStackImpl")
				|| logEntry.contains("CfwTCPListene")
				|| logEntry.contains("Peer")
				|| logEntry.contains("FailureScopeC")
				|| logEntry.contains("ServerCollabo")
				|| logEntry.contains("ObjectManager")
				|| logEntry.contains("AppProfileCom")
				|| logEntry.contains("ActivitySessi")
				|| logEntry.contains("TransportAdap")
				|| logEntry.contains("DiscoveryRcv")
				|| logEntry.contains("VSyncAlgo1")
				|| logEntry.contains("P2PGroup")) {
			msg = logEntry.substring(logEntry.indexOf(direction));
		} else {
			motifSystemOut = Pattern.compile("\\[CID.*\\]|\\[\\]");
			try {
				Matcher matcherMsg = motifSystemOut.matcher(logEntry);
				if(matcherMsg.find()){
					msg = logEntry.substring(matcherMsg.end() + 1);
				}
				
			} catch (IllegalStateException e) {
				e.printStackTrace();
				msg = "-";
			}
		}
		// parsing queris inside of msg
		String query = "-";
		String consernedTables = "-";
		String execution = "-";
		int executionTime = 0;
		try {
			Pattern queriesMotif = Pattern
					.compile("((select.*from.*)|(insert into.*values.*)|(update.*set.*)|(delete from.*)|(alter.*))");
			Matcher queriesMatcher = queriesMotif.matcher(msg);
			if (queriesMatcher.find()) {
				isQueryEntry = true;
				String queryLine = msg.substring(
						queriesMatcher.start(), queriesMatcher.end());

				if (queryLine.contains("execution took")) {
					queriesMotif = Pattern
							.compile(".*(?= \\(execution took.*\\))");
					queriesMatcher = queriesMotif.matcher(queryLine);
					if(queriesMatcher.find()){
						query = queryLine.substring(queriesMatcher.start(),
								queriesMatcher.end());
					}
				} else {
					query = queryLine;
				}

				// conserned tables
				queriesMotif = Pattern
						.compile("((?<=from ).*(?= where))|((?<=insert into).*(?= values))|((?<=update ).*(?= set))|((?<=alter ).*(?= (alter|modify|add|drop)))");
				queriesMatcher = queriesMotif.matcher(query);
				if(queriesMatcher.find()){
					consernedTables = query.substring(
							queriesMatcher.start(), queriesMatcher.end());
				}

				// execution time
				try {
					queriesMotif = Pattern
							.compile("\\(execution took [\\d]+.*\\)");
					queriesMatcher = queriesMotif.matcher(queryLine);
					if(queriesMatcher.find()){
						execution = queryLine.substring(queriesMatcher.start(),
								queriesMatcher.end());
						executionTime = Integer.parseInt(execution.replaceAll(
								"\\D+", ""));
					}
					
				} catch (IllegalStateException e) {
					executionTime = 0;
					e.printStackTrace();
				}
			}
		} catch (IllegalStateException e) {
			e.printStackTrace();
		}
		
		//exceptions
		String exceptionMsg = "";
		String exceptionClass = "";
		if(logEntry.contains("Exception")){
			isExceptionEntry = true;
			try {
				Pattern exceptionMotif = Pattern.compile("(\\s|\n).*Exception", Pattern.DOTALL);
				Matcher exceptionMatcher = exceptionMotif.matcher(logEntry);
				if(exceptionMatcher.find()){
					exceptionMsg = logEntry.substring(exceptionMatcher.end());
				}
				
				exceptionMotif = Pattern.compile("[a-zA-Z\\.]*Exception");
				exceptionMatcher = exceptionMotif.matcher(logEntry);
				if(exceptionMatcher.find()){
					exceptionClass = logEntry.substring(exceptionMatcher.start(), exceptionMatcher.end());
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		ObservableList<Object> parsedLogQuery = FXCollections.observableArrayList();
		ObservableList<Object> parsedLogException = FXCollections.observableArrayList();
		if (isQueryEntry) {
			parsedLogQuery.addAll(query, consernedTables, executionTime);
		}
		if(isExceptionEntry){
			parsedLogException.addAll(mySQLDate, level, exceptionClass, exceptionMsg);
		}
		
		parsedLogString.get(0).add(parsedLogQuery);
		parsedLogString.get(1).add(parsedLogException);
		return parsedLogString;
	}
	
	
	public Vector<ObservableList<ObservableList<Object>>> parseSystemOutFileContent(String fileContent){
		Vector<ObservableList<ObservableList<Object>>> parsedFileContent = new Vector<ObservableList<ObservableList<Object>>>(); 
		ObservableList<ObservableList<Object>> parsedLogQueries = FXCollections.observableArrayList();
		ObservableList<ObservableList<Object>> parsedLogExceptions = FXCollections.observableArrayList();
		parsedFileContent.add(parsedLogQueries);
		parsedFileContent.add(parsedLogExceptions);
		
			Pattern motifSystemOut = Pattern.compile("\\[\\d.*?(?=\\[\\d|\\n)", Pattern.DOTALL);/*\\[\\d.*?(?=\\[\\d)*/
			Matcher matcher = motifSystemOut.matcher(fileContent);
			int nbEntries = 0;
			while (matcher.find()) {
				nbEntries++;
				String logEntry = fileContent.substring(matcher.start(), matcher.end());
				Vector<ObservableList<ObservableList<Object>>> parsedLogEntry = parseSystemOutString(logEntry);
				parsedFileContent.get(0).add(parsedLogEntry.get(0).get(0));
				parsedFileContent.get(1).add(parsedLogEntry.get(1).get(0));
			}
		return parsedFileContent;
	}

/*public static void main(String[] args) {
		

		File logFile = new File(("D:\\Ingenieur\\formations\\stage\\Smartech_2015\\files\\logs\\MXServer\\SystemOut.log"));
		try {
			String logFileContent = FileUtils.readFileToString(logFile, "UTF-8");
			long currentTime = System.currentTimeMillis();
			LogFileParser logFileParser = new LogFileParser(logFile);
			
				
			System.out.println(logFileParser.parseSystemOutFileContent(logFileContent).get(0).get(0));
			ObservableList<ObservableList<Object>> logQueries = logFileParser.parseSystemOutFileContent(logFileContent).get(0);
			ObservableList<ObservableList<Object>> logExceptions = logFileParser.parseSystemOutFileContent(logFileContent).get(1);
			for(ObservableList<Object> logQuery : logQueries){
				if(!logQuery.isEmpty()){
					System.out.println(logQuery);
				}
			}
			
			
			System.out.println("\n\n"+(System.currentTimeMillis()- currentTime));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	
	}*/
}

	
