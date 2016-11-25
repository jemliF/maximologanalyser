package com.smartech.loganalyser.src.view;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Pagination;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.web.HTMLEditor;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.util.Callback;
import javafx.util.Duration;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.io.FileUtils;
import org.controlsfx.control.Notifications;
import org.controlsfx.control.RangeSlider;
import org.controlsfx.control.TaskProgressView;
import org.controlsfx.dialog.Dialogs;
import com.smartech.loganalyser.src.db.MaximoParameteredConnection;
import com.smartech.loganalyser.src.utils.LogFileParser;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class MainController implements Initializable {

    @FXML
    private TabPane logAnalyserTabPane;
    @FXML
    private Tab queryTesterTab;
    @FXML
    private Tab searchTab;
    @FXML
    private TextField searchInFilesTextField;
    @FXML
    private MenuItem searchMenuItem;
    @FXML
    private MenuItem refreshSelectedFileMenuItem;
    @FXML
    private CheckBox searchInFilesSaveCSVCheckBox;
    @FXML
    private TableView searchInFilesTableView;
    @FXML
    private TextField searchInExceptionsTextField;
    @FXML
    private TextField searchInQueriesTextField;
    @FXML
    private TreeView<String> logDirectory;
    @FXML
    private TableView queriesTableView;
    @FXML
    private TaskProgressView taskProgressView;
    @FXML
    private CheckBox saveExceptionsInCSVCheckBox;
    @FXML
    private CheckBox saveQueriesInCSVCheckBox;
    @FXML
    private Button btnReload;
    @FXML
    private TextArea fileContentTextArea;
    @FXML
    private AnchorPane queryResultAnchorPane;
    @FXML
    private ScrollPane queryResultScrollPane;
    private String selectedFileContent = "";
    private List<File> mxFolders;
    private String logDirectoryPath = "";
    private String ibmLocationPath;
    private LogFileParser logFileParser;
    private File selectedFile;
    private ExecutorService executorService = Executors.newCachedThreadPool();
    private ObservableList<ObservableList<Object>> queriesTableViewData = FXCollections
            .observableArrayList();
    private ObservableList<TableColumn> queriesTableViewColumns = FXCollections
            .observableArrayList();
    @FXML
    private HTMLEditor selectedQueryHtmlEditor;
    private List<TreeItem<String>> mxItems;
    @FXML
    private TableView exceptionsTableView;
    private ObservableList<ObservableList<Object>> exceptionsTableViewData = FXCollections
            .observableArrayList();
    private ObservableList<TableColumn> exceptionsTableViewColumns = FXCollections
            .observableArrayList();
    private String selectedQuery = "";
    @FXML
    private Label executionTimeLabel;
    private List<String> selectedFileLines = new ArrayList<String>();
    private Vector<ObservableList<ObservableList<Object>>> parsedSelectedFileContent = new Vector<ObservableList<ObservableList<Object>>>();
    private ObservableList<ObservableList<Object>> logQueries = FXCollections.observableArrayList();
    private ObservableList<ObservableList<Object>> logExceptions = FXCollections.observableArrayList();
    private TreeItem<String> selectedItem;
    private TableView queryResultTableView;
    private TreeItem<String> rootItem;
    private TreeItem<String> mxserverItem;
    private TreeItem<String> mxserversndpItem;
    private int selectedFileContentPageCount = 0;
    private int selectedFileContentCurrentPageNumber;
    private RangeSlider executionTimeRangeSlider = new RangeSlider(0, 100, 10, 90);
    @FXML
    private TextField executionTime1TextField;
    @FXML
    private TextField executionTime2TextField;
    @FXML
    private AnchorPane executionTimeAnchorPane;
    @FXML
    private HBox box;
    @FXML
    private Pagination selectedFileContentPagination;
    @FXML
    private TextField searchInSelectedFileContentTextField;
    @FXML
    private ProgressBar progressBar;

    public void openSearchTab() {
        logAnalyserTabPane.getSelectionModel()
                .select(searchTab);
    }
    private File savedLogFiles = new File("./csv/log");
    private Vector<Integer> selectedPageNumbers = new Vector<Integer>();

    class SearchInSelectedLocationTask extends Task<Void> {

        @SuppressWarnings("unchecked")
        @Override
        protected Void call() throws Exception {
            String searchedTextInSelectedFile = searchInFilesTextField.getText();
            ObservableList<ObservableList<String>> searchTextInFileResults = searchTextInFile(searchedTextInSelectedFile, selectedFile);
            searchInFilesTableView.setItems(searchTextInFileResults);

            if (searchInFilesSaveCSVCheckBox.isSelected()) {

                Date date = new Date();
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss");

                FileWriter fileWriter = null;
                CSVPrinter csvFilePrinter = null;
                try {
                    Object[] FILE_HEADER = {"File name", "Line number", "Line"};

                    CSVFormat csvFileFormat = CSVFormat.DEFAULT.withRecordSeparator("\n");
                    fileWriter = new FileWriter("./csv/search/text/" + searchedTextInSelectedFile + "_" + dateFormat.format(date) + ".csv");
                    csvFilePrinter = new CSVPrinter(fileWriter, csvFileFormat);
                    csvFilePrinter.printRecord(FILE_HEADER);
                    for (ObservableList<String> searchTextInFileResult : searchTextInFileResults) {
                        csvFilePrinter.printRecord(searchTextInFileResult);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    try {
                        fileWriter.flush();
                        fileWriter.close();
                        csvFilePrinter.close();
                    } catch (Exception e2) {
                        e2.printStackTrace();
                    }
                }
            }
            return null;
        }

    }

    public ObservableList<ObservableList<String>> searchTextInFile(String text, File file) {
        ObservableList<ObservableList<String>> searchResults = FXCollections.observableArrayList();
        if (file.isFile()) {
            System.out.println("file selected");
            try {
                List<String> fileLines = FileUtils.readLines(file, "UTF-8");
                for (String line : fileLines) {
                    if (line.contains(text)) {
                        ObservableList<String> searchResult = FXCollections.observableArrayList();
                        searchResult.add(file.getName());
                        searchResult.add("" + fileLines.indexOf(line));
                        searchResult.add(line);
                        searchResults.add(searchResult);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("directory selected");
            for (File innerFile : file.listFiles()) {
                System.out.println(innerFile.getAbsolutePath());
                System.out.println(searchTextInFile(text, innerFile).size());
                searchResults.addAll(searchTextInFile(text, innerFile));
            }
        }
        return searchResults;
    }

    public void searchInSelectedLocation() {
        SearchInSelectedLocationTask searchInSelectedLocationTask = new SearchInSelectedLocationTask();
        taskProgressView.getTasks().add(searchInSelectedLocationTask);
        executorService.submit(searchInSelectedLocationTask);
    }

    public void searchInExceptions() {
        parsedSelectedFileContent = new LogFileParser().parseSystemOutFileContent(selectedFileContent);
        Date date = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss");
        if (searchInExceptionsTextField.getText() != null) {
            String searchedText = searchInExceptionsTextField.getText();
            if (saveExceptionsInCSVCheckBox.isSelected()) {                         //Save search results in CSV file
                exceptionsTableViewData = FXCollections.observableArrayList();

                logExceptions = parsedSelectedFileContent.get(1);
                ObservableList<Object> exceptionsTableViewRow = FXCollections
                        .observableArrayList();
                FileWriter fileWriter = null;
                CSVPrinter csvFilePrinter = null;
                Object[] FILE_HEADER = {"Date and time", "Level", "Class", "Message"};
                try {
                    CSVFormat csvFileFormat = CSVFormat.DEFAULT.withRecordSeparator("\n");
                    fileWriter = new FileWriter("./csv/search/exceptions/" + searchedText + "_" + dateFormat.format(date) + ".csv");
                    csvFilePrinter = new CSVPrinter(fileWriter, csvFileFormat);
                    csvFilePrinter.printRecord(FILE_HEADER);
                    for (ObservableList<Object> logException : logExceptions) {
                        if (!logException.isEmpty() && (logException.get(0).toString().contains(searchedText) || logException.get(1).toString().contains(searchedText) || logException.get(2).toString().contains(searchedText) || logException.get(3).toString().contains(searchedText))) {
                            exceptionsTableViewRow.add(logException.get(0).toString());
                            exceptionsTableViewRow.add(logException.get(1));
                            exceptionsTableViewRow.add(logException.get(2));
                            exceptionsTableViewRow.add(logException.get(3));
                            exceptionsTableViewData.add(exceptionsTableViewRow);
                            csvFilePrinter.printRecord(exceptionsTableViewRow);

                            exceptionsTableViewRow = FXCollections.observableArrayList();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    try {
                        fileWriter.flush();
                        fileWriter.close();
                        csvFilePrinter.close();
                    } catch (Exception e2) {
                        e2.printStackTrace();
                    }
                }
                exceptionsTableView.setItems(exceptionsTableViewData);
            } else {                                                                //Display search results
                exceptionsTableViewData = FXCollections.observableArrayList();

                logExceptions = parsedSelectedFileContent.get(1);
                ObservableList<Object> exceptionsTableViewRow = FXCollections
                        .observableArrayList();
                for (ObservableList<Object> logException : logExceptions) {
                    if (!logException.isEmpty() && (logException.get(0).toString().contains(searchedText) || logException.get(1).toString().contains(searchedText) || logException.get(2).toString().contains(searchedText) || logException.get(3).toString().contains(searchedText))) {
                        exceptionsTableViewRow.add(logException.get(0).toString());
                        exceptionsTableViewRow.add(logException.get(1));
                        exceptionsTableViewRow.add(logException.get(2));
                        exceptionsTableViewRow.add(logException.get(3));
                        exceptionsTableViewData.add(exceptionsTableViewRow);

                        exceptionsTableViewRow = FXCollections.observableArrayList();
                    }
                }
                exceptionsTableView.setItems(exceptionsTableViewData);
            }
        }
    }

    public void searchInQueries() {
        parsedSelectedFileContent = new LogFileParser().parseSystemOutFileContent(selectedFileContent);
        String searchedText = searchInQueriesTextField.getText();
        Date date = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss");
        int minExecutionTime = 0;
        int maxExecutionTime = Integer.MAX_VALUE;
        try {
            minExecutionTime = Integer.parseInt(executionTime1TextField.getText());
        } catch (Exception e) {
            if (!executionTime1TextField.getText().isEmpty()) {
                Notifications.create()
                        .title("Format error")
                        .text("Min execution time must be numeric").position(Pos.CENTER)
                        .showError();
                minExecutionTime = 0;
            }
            e.printStackTrace();
        }
        try {
            maxExecutionTime = Integer.parseInt(executionTime2TextField.getText());
        } catch (Exception e) {
            if (!executionTime2TextField.getText().isEmpty()) {
                Notifications.create()
                        .title("Format error")
                        .text("Max execution time must be numeric").position(Pos.CENTER)
                        .showError();
                maxExecutionTime = Integer.MAX_VALUE;
            }
            e.printStackTrace();
        }
        if (saveQueriesInCSVCheckBox.isSelected()) {                            //Save results in CSV file
            //display results
            queriesTableViewData = FXCollections.observableArrayList();
            logQueries = parsedSelectedFileContent.get(0);
            ObservableList<Object> queriesTableViewRow = FXCollections
                    .observableArrayList();
            FileWriter fileWriter = null;
            CSVPrinter csvFilePrinter = null;
            Object[] FILE_HEADER = {"Query", "Conserned tables", "Execution time"};
            try {
                CSVFormat csvFileFormat = CSVFormat.DEFAULT.withRecordSeparator("\n");
                fileWriter = new FileWriter("./csv/search/queries/" + searchedText + "_" + dateFormat.format(date) + ".csv");
                csvFilePrinter = new CSVPrinter(fileWriter, csvFileFormat);
                csvFilePrinter.printRecord(FILE_HEADER);
                for (ObservableList<Object> logQuery : logQueries) {
                    if (!logQuery.isEmpty()) {
                        int executionTime = (Integer) logQuery.get(2);
                        if ((logQuery.get(0).toString().contains(searchedText) || logQuery.get(1).toString().contains(searchedText) || logQuery.get(2).toString().contains(searchedText)) && (executionTime >= minExecutionTime && executionTime <= maxExecutionTime)) {
                            queriesTableViewRow.add(logQuery.get(0));
                            queriesTableViewRow.add(logQuery.get(1));
                            queriesTableViewRow.add(logQuery.get(2));
                            queriesTableViewData.add(queriesTableViewRow);
                            csvFilePrinter.printRecord(queriesTableViewRow);

                            queriesTableViewRow = FXCollections.observableArrayList();
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    fileWriter.flush();
                    fileWriter.close();
                    csvFilePrinter.close();
                } catch (Exception e2) {
                    e2.printStackTrace();
                }
            }
            queriesTableView.setItems(queriesTableViewData);
        } else {
            //only display results
            queriesTableViewData = FXCollections.observableArrayList();
            logQueries = parsedSelectedFileContent.get(0);
            ObservableList<Object> queriesTableViewRow = FXCollections
                    .observableArrayList();

            for (ObservableList<Object> logQuery : logQueries) {
                if (!logQuery.isEmpty()) {
                    int executionTime = (Integer) logQuery.get(2);
                    if ((logQuery.get(0).toString().contains(searchedText) || logQuery.get(1).toString().contains(searchedText) || logQuery.get(2).toString().contains(searchedText)) && (executionTime >= minExecutionTime && executionTime <= maxExecutionTime)) {
                        queriesTableViewRow.add(logQuery.get(0));
                        queriesTableViewRow.add(logQuery.get(1));
                        queriesTableViewRow.add(logQuery.get(2));
                        queriesTableViewData.add(queriesTableViewRow);
                        queriesTableViewRow = FXCollections.observableArrayList();
                    }
                }
            }
            queriesTableView.setItems(queriesTableViewData);
        }
    }

    public void refreshSelectedFile() {
        if (selectedItem.isLeaf()) {
            selectedFile = new File(logDirectoryPath
                    + "\\"
                    + selectedItem.getParent()
                    .getValue() + "\\"
                    + selectedItem.getValue());
            System.out.println("selectedFile: " + selectedFile);
            onFileSelected();
        }
    }

    public void refrechLogFilesTreeView() {
        //Eliminate files no longer exist
        for (TreeItem<String> item : mxItems) {
            File[] mxLogFiles = new File(logDirectoryPath + "\\" + item.getValue()).listFiles();
            boolean exist = false;
            Vector<TreeItem> toRemoveItems = new Vector<TreeItem>();
            for (TreeItem<String> logItem : item.getChildren()) {
                exist = false;
                for (File logFile : mxLogFiles) {
                    if (logFile.getName().equals(logItem.getValue())) {
                        exist = true;
                    }
                }
                if (!exist) {
                    toRemoveItems.add(logItem);
                    System.out.println("removed logItem: " + logItem);
                }
            }
            for (TreeItem itemToRemove : toRemoveItems) {
                if (itemToRemove != null) {
                    item.getChildren().remove(itemToRemove);
                    if (selectedFile != null) {
                        if (itemToRemove.getValue().equals(selectedFile.getName())) {
                            logDirectory.getSelectionModel().clearSelection();
                        }
                    }
                }
            }
        }

        //Add new files
        for (File mxFolder : mxFolders) {
            TreeItem<String> mxItem = mxItems.get(mxFolders.indexOf(mxFolder));
            for (File mxLogFile : mxFolder.listFiles()) {
                if (mxLogFile.getName().endsWith(".log") && mxLogFile.getName().contains("System")) {
                    boolean exist = false;
                    for (TreeItem<String> mxLogItem : mxItem.getChildren()) {
                        if (mxLogItem.getValue().equals(mxLogFile.getName())) {
                            exist = true;
                        }
                    }
                    if (!exist) {
                        TreeItem<String> newLogFileItem = new TreeItem<String>(mxLogFile.getName());
                        mxItem.getChildren().add(newLogFileItem);
                        System.out.println("new LogFileItem: " + newLogFileItem);
                    }
                }
            }
        }
        logDirectory.getSelectionModel().select(selectedItem);
    }

    @SuppressWarnings("unchecked")
    public void onFileSelected() {
        // progressBar.setProgress(0);

        long currentTime = System.currentTimeMillis();
        try {
            selectedFileLines = FileUtils.readLines(selectedFile, "UTF-8");
            System.out.println("spended time: " + (System.currentTimeMillis() - currentTime));
            if (selectedFileLines.size() % 1000 == 0) {
                selectedFileContentPageCount = selectedFileLines.size() / 1000;
            } else {
                selectedFileContentPageCount = (selectedFileLines.size() / 1000) + 1;
            }

            if (selectedFileContentPageCount == 0) {
                selectedFileContentPageCount = 1;
            }
            selectedFileContentPagination.setPageCount(selectedFileContentPageCount);
            selectedFileContentCurrentPageNumber = 1;

            Task<Void> displayerTask = new Task() {
                @Override
                protected Object call() throws Exception {
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            if (selectedFileLines.size() >= 999) {
                                int i = 0;
                                for (i = 0; i < 1000; i++) {
                                    fileContentTextArea.appendText(selectedFileLines.get(i) + "\n");
                                    final double step = i;
                                    updateProgress(i, 1000);
                                    //Platform.runLater(() -> progressBar.setProgress(step / 1000F));
                                }
                            } else {
                                int i = 0;
                                for (i = 0; i < selectedFileLines.size(); i++) {
                                    fileContentTextArea.appendText(selectedFileLines.get(i) + "\n");
                                    final double step = i;
                                    updateProgress(i, selectedFileLines.size());
                                    //Platform.runLater(() -> progressBar.setProgress(step / selectedFileLines.size()));
                                }
                            }
                        }
                    });
                    return null;
                }
            };
            progressBar.progressProperty().bind(displayerTask.progressProperty());
            taskProgressView.getTasks().add(displayerTask);
            executorService.submit(displayerTask);

            System.out.println("spended filling time: " + (System.currentTimeMillis() - currentTime));
            final String textAreaContent = fileContentTextArea.getText();
            Task<Void> parserTask = new Task() {
                @Override
                protected Object call() throws Exception {
                    selectedFileContent = FileUtils.readFileToString(selectedFile, "UTF-8");
                    parsedSelectedFileContent = new LogFileParser().parseSystemOutFileContent(textAreaContent);
                    return null;
                }
            };
            taskProgressView.getTasks().add(parserTask);
            executorService.submit(parserTask);

            parserTask.setOnSucceeded(new EventHandler<WorkerStateEvent>() {

                @Override
                public void handle(WorkerStateEvent arg0) {
                    displaySelectedFileQueries();
                    displaySelectedFileExceptions();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("spended global time : " + (System.currentTimeMillis() - currentTime));
    }

    @SuppressWarnings("unchecked")
    public void displaySelectedFileExceptions() {									//DISPLAY EXCEPTIONS
        exceptionsTableViewData = FXCollections.observableArrayList();

        logExceptions = parsedSelectedFileContent.get(1);
        ObservableList<Object> exceptionsTableViewRow = FXCollections
                .observableArrayList();
        for (ObservableList<Object> logException : logExceptions) {
            if (!logException.isEmpty()) {
                exceptionsTableViewRow.add(logException.get(0).toString());
                exceptionsTableViewRow.add(logException.get(1));
                exceptionsTableViewRow.add(logException.get(2));
                exceptionsTableViewRow.add(logException.get(3));
                exceptionsTableViewData.add(exceptionsTableViewRow);

                exceptionsTableViewRow = FXCollections.observableArrayList();
            }
        }
        exceptionsTableView.setItems(exceptionsTableViewData);
    }

    @SuppressWarnings("unchecked")
    public void displaySelectedFileQueries() {										//DISPLAY QUERIES
        queriesTableViewData = FXCollections.observableArrayList();

        logQueries = parsedSelectedFileContent.get(0);
        ObservableList<Object> queriesTableViewRow = FXCollections
                .observableArrayList();
        for (ObservableList<Object> logQuery : logQueries) {
            if (!logQuery.isEmpty()) {
                queriesTableViewRow.add(logQuery.get(0));
                queriesTableViewRow.add(logQuery.get(1));
                queriesTableViewRow.add(logQuery.get(2));
                queriesTableViewData.add(queriesTableViewRow);
                queriesTableViewRow = FXCollections.observableArrayList();
            }
        }
        queriesTableView.setItems(queriesTableViewData);
    }

    @SuppressWarnings("unchecked")
    public void testSelectedQuery() {
        checkPropertiesFile();
        MaximoParameteredConnection maximoParameteredConnection = null;
        queryResultAnchorPane.getChildren().clear();
        System.out.println("\n\nselected query: " + selectedQuery + "\n\n");
        /*selectedQuery = selectedQueryHtmlEditor.getHtmlText();
         stripHTMLTags(selectedQuery);*/
        if (selectedQuery.startsWith("select") || selectedQuery.startsWith("SELECT")) {//select query
            maximoParameteredConnection = new MaximoParameteredConnection();
            long currentTimeMillis = System.currentTimeMillis();
            ResultSet resultSet = maximoParameteredConnection.selectQuery(selectedQuery);
            executionTimeLabel.setText("execution time: " + (System.currentTimeMillis() - currentTimeMillis) + " (ms)");
            queryResultTableView = new TableView();
            queryResultAnchorPane.getChildren().add(queryResultTableView);
            //queryResultScrollPane.setContent(queryResultTableView);
            ObservableList<ObservableList<Object>> queryResultTableViewData = FXCollections.observableArrayList();
            ObservableList<TableColumn> queryResultTableViewColumns = FXCollections.observableArrayList();
            try {
                ResultSetMetaData resultSetMetaData = (ResultSetMetaData) resultSet.getMetaData();
                queryResultTableView.setPrefWidth(75 * resultSetMetaData.getColumnCount());
                for (int i = 0; i < resultSetMetaData.getColumnCount(); i++) {
                    final int j = i;
                    TableColumn col = new TableColumn(resultSetMetaData.getColumnName(i + 1));
                    col.setMinWidth(1000 / resultSetMetaData.getColumnCount());
                    col.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<ObservableList, String>, ObservableValue<String>>() {
                        public ObservableValue<String> call(
                                TableColumn.CellDataFeatures<ObservableList, String> param) {
                            return new SimpleStringProperty(param.getValue().get(j)
                                    .toString());
                        }
                    });

                    queryResultTableViewColumns.add(col);
                }
                queryResultTableView.getColumns().setAll(queryResultTableViewColumns);
                queryResultTableView.prefWidthProperty().bind(queryResultAnchorPane.prefWidthProperty());
                queryResultTableView.prefHeightProperty().bind(queryResultAnchorPane.prefHeightProperty());

                while (resultSet.next()) {
                    ObservableList<Object> queryResultRow = FXCollections.observableArrayList();
                    for (int k = 1; k <= resultSetMetaData.getColumnCount(); k++) {
                        if (resultSet.getObject(k) != null) {
                            queryResultRow.add(resultSet.getObject(k));
                        } else {
                            queryResultRow.add("-");
                        }
                    }
                    queryResultTableViewData.add(queryResultRow);
                }
                queryResultTableView.setItems(queryResultTableViewData);
            } catch (Exception e) {
                e.printStackTrace();
                if (e.getClass().getName().equals("java.net.ConnectException") || e.getClass().getName().equals("java.net.ConnectException")) {
                    Dialogs.create().owner(null).title("Error occured")
                            .masthead("Connection failed")
                            .message("Connection failed due to network troubles")
                            .showError();
                }
            }
        } else {//update query

        }
        if (maximoParameteredConnection != null) {
            maximoParameteredConnection.closeConnection();
        }
    }

    public File browseDatabasePropertiesJar() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("properties.jar file");
        File defaultDirectory = new File("C:/");
        fileChooser.setInitialDirectory(defaultDirectory);
        File selectedJarFile = fileChooser.showOpenDialog(null);
        if (selectedJarFile != null && selectedJarFile.exists() && selectedJarFile.getName().endsWith(".jar")) {
            return selectedJarFile;
        } else {
            Dialogs.create().owner(null).title("Error occured")
                    .masthead("properties.jar location")
                    .message("Selected file is not properties.jar file")
                    .showError();
        }
        return selectedJarFile;
    }

    public void checkPropertiesFile() {
        File propertiesJarFile = new File("properties.jar");
        if (!propertiesJarFile.exists()) {
            propertiesJarFile = browseDatabasePropertiesJar();
            if (propertiesJarFile != null) {
                try {
                    Files.copy(propertiesJarFile.toPath(), new File(
                            ".\\properties.jar").toPath());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }

        File propertiesFile = new File("./maximo.properties");
        if (!propertiesFile.exists()) {
            System.out.println("file do not exist");
            try {
                java.util.jar.JarFile jar = new java.util.jar.JarFile(
                        "./properties.jar");

                java.util.Enumeration enumEntries = jar.entries();
                while (enumEntries.hasMoreElements()) {
                    java.util.jar.JarEntry file = (java.util.jar.JarEntry) enumEntries
                            .nextElement();
                    System.out.println("jar entry:" + file.getName());
                    if (file.getName().contains("maximo.properties")) {
                        java.io.File f = new java.io.File(file.getName());
                        java.io.InputStream is = jar.getInputStream(file);
                        java.io.FileOutputStream fos = new java.io.FileOutputStream(
                                f);
                        while (is.available() > 0) {
                            fos.write(is.read());
                        }
                        fos.close();
                        is.close();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void initialize(URL arg0, ResourceBundle arg1) {
        Platform.runLater(new Runnable() {
            @SuppressWarnings("unchecked")
            public void run() {
                buildExceptionsTableView();
                buildQueriesTableView();
                buildSearchInFilesResultsTableView();
                locateLogDirectory();
                buildLogDirectoryTreeView();

                searchInSelectedFileContentTextField.setOnKeyPressed(new EventHandler<KeyEvent>() {
                    @Override
                    public void handle(KeyEvent ke) {
                        if (ke.getCode().equals(KeyCode.ENTER)) {
                            System.out.println("enter in file content textField");
                            executorService.submit(new Task<Void>() {
                                @Override
                                protected Void call() throws Exception {
                                    String searchText = searchInSelectedFileContentTextField.getText();
                                    Pattern motif = Pattern.compile(searchText);
                                    Matcher matcher = motif.matcher(fileContentTextArea.getText());
                                    while (matcher.find()) {
                                        System.out.println(matcher.start());
                                        fileContentTextArea.selectRange(matcher.start(), matcher.end());
                                    }

                                    return null;
                                }
                            });
                        }
                    }
                });

                selectedFileContentPagination.currentPageIndexProperty().addListener(new ChangeListener<Number>() {

                    @Override
                    public void changed(ObservableValue<? extends Number> observable,
                            Number oldValue, Number newValue) {
                        System.out.println(newValue);
                        fileContentTextArea.setText("");
                        selectedFileContentCurrentPageNumber = newValue.intValue();
                        selectedPageNumbers.add(selectedFileContentCurrentPageNumber);

                        if (selectedFileContentCurrentPageNumber == selectedFileContentPageCount - 1) {
                            for (int i = 1000 * selectedFileContentCurrentPageNumber; i <= selectedFileLines.size() - 1; i++) {
                                fileContentTextArea.appendText(selectedFileLines.get(i) + "\n");

                            }
                        } else {
                            for (int i = 1000 * selectedFileContentCurrentPageNumber; i < 1000 * (selectedFileContentCurrentPageNumber + 1); i++) {
                                fileContentTextArea.appendText(selectedFileLines.get(i) + "\n");
                            }
                        }
                        parsedSelectedFileContent = new Vector<ObservableList<ObservableList<Object>>>();
                        parsedSelectedFileContent = new LogFileParser().parseSystemOutFileContent(fileContentTextArea.getText());

                        displaySelectedFileQueries();
                        displaySelectedFileExceptions();
                    }
                });

                // check properties.jar file: copy it if is not in the project root
                checkPropertiesFile();

                System.out.println("log directory path:" + logDirectoryPath);
                /*logDirectory.getSelectionModel().selectedItemProperty()
                 .addListener(new ChangeListener() {
                
                 public void changed(ObservableValue observable,
                 Object oldValue, Object newValue) {
                
                 selectedItem = (TreeItem<String>) newValue;
                 if (selectedItem != null) {
                 selectedFile = new File(logDirectoryPath
                 + "\\" + selectedItem.getValue());
                
                 System.out.println("selectedFile: " + selectedFile);
                 if (selectedItem.isLeaf()) {
                 selectedFile = new File(logDirectoryPath
                 + "\\"
                 + selectedItem.getParent()
                 .getValue() + "\\"
                 + selectedItem.getValue());
                 System.out.println("selectedFile: " + selectedFile);
                 onFileSelected();
                 }
                 }
                
                 }
                 });*/

                queriesTableView
                        .setOnMousePressed(new EventHandler<MouseEvent>() {

                            public void handle(MouseEvent event) {
                                if (event.isPrimaryButtonDown()
                                && event.getClickCount() == 2) {
                                    queryResultTableView = new TableView();
                                    selectedQueryHtmlEditor.setHtmlText("<html><body>");
                                    ObservableList<Object> queriesTableViewSelectedRow = FXCollections
                                    .observableArrayList();
                                    queriesTableViewSelectedRow = (ObservableList<Object>) queriesTableView
                                    .getSelectionModel()
                                    .getSelectedItems().get(0);
                                    selectedQuery = (String) queriesTableViewSelectedRow
                                    .get(0);
                                    if (selectedQuery.startsWith("select") || selectedQuery.startsWith("SELECT")) {
                                        System.out.println("selected query: "
                                                + selectedQuery);
                                        logAnalyserTabPane.getSelectionModel()
                                        .select(queryTesterTab);
                                        String[] queryWords = selectedQuery
                                        .split("[\\s,;\\n\\t\\.]+");
                                        Pattern sqlKeyWordMotif = Pattern
                                        .compile("(select|from|where|order|by|group|by|insert|into|outer|values|update|set|delete|alter|drop|modify|add|having|count|constraint|primary key|desc|asc|and|in|inner|join|by|like|or|on|max|min)");
                                        for (int i = 0; i < queryWords.length; i++) {
                                            Matcher sqlKeyWordMatcher = sqlKeyWordMotif
                                            .matcher(queryWords[i]);
                                            if (sqlKeyWordMatcher.find()
                                            && (queryWords[i].equals(queryWords[i].substring(sqlKeyWordMatcher.start(), sqlKeyWordMatcher.end())))) {
                                                System.out.println(queryWords[i]);
                                                selectedQueryHtmlEditor.setHtmlText(selectedQueryHtmlEditor.getHtmlText() + "<strong><font color='teal'>" + queryWords[i] + "</font></strong>" + " ");

                                            } else {
                                                selectedQueryHtmlEditor.setHtmlText(selectedQueryHtmlEditor.getHtmlText() + queryWords[i] + " ");
                                            }
                                        }
                                        selectedQueryHtmlEditor.setHtmlText(selectedQueryHtmlEditor.getHtmlText() + "</body></html>");
                                    }
                                }
                            }
                        });
                Timeline logTreeViewrefresh = new Timeline(new KeyFrame(Duration.seconds(5), new EventHandler<ActionEvent>() {

                    @Override
                    public void handle(ActionEvent event) {
                        refrechLogFilesTreeView();
                    }
                }));
                logTreeViewrefresh.setCycleCount(Timeline.INDEFINITE);
                logTreeViewrefresh.play();

                exceptionsTableView.setOnMousePressed(new EventHandler<MouseEvent>() {
                    public void handle(MouseEvent event) {
                        if (event.isPrimaryButtonDown()
                                && event.getClickCount() == 2) {
                            ObservableList<Object> exceptionsTableViewSelectedRow = FXCollections
                                    .observableArrayList();
                            exceptionsTableViewSelectedRow = (ObservableList<Object>) exceptionsTableView
                                    .getSelectionModel()
                                    .getSelectedItems().get(0);
                            TextArea exceptionTextArea = new TextArea();
                            exceptionTextArea.appendText("\n" + exceptionsTableViewSelectedRow.get(3).toString());
                            exceptionTextArea.setWrapText(true);
                            TextField classLabel = new TextField(exceptionsTableViewSelectedRow.get(2).toString());
                            /*Dialog exceptionDialog = new Dialog(null, "Exception stack trace");
                             exceptionDialog.setExpandableContent(exceptionTextArea);
                             exceptionDialog.setMasthead(exceptionsTableViewSelectedRow.get(2).toString());
                             exceptionDialog.show();*/

                            Stage dialog = new Stage();
                            dialog.initStyle(StageStyle.UTILITY);
                            Scene scene = new Scene(new Group(new StackPane(new VBox(classLabel, exceptionTextArea))));
                            dialog.setScene(scene);

                            dialog.show();
                        }
                    }
                });
            }
        });
    }

    public void onlogFileCliqued() {
        selectedItem = logDirectory.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            selectedFile = new File(logDirectoryPath
                    + "\\" + selectedItem.getValue());

            System.out.println("selectedFile: " + selectedFile);
            if (selectedItem.isLeaf()) {
                selectedFile = new File(logDirectoryPath
                        + "\\"
                        + selectedItem.getParent()
                        .getValue() + "\\"
                        + selectedItem.getValue());
                System.out.println("selectedFile: " + selectedFile);
                onFileSelected();
            }
        }
    }

    @SuppressWarnings("unchecked")
    public void buildExceptionsTableView() {
        exceptionsTableViewColumns = FXCollections.observableArrayList();

        TableColumn colDateTime = new TableColumn("Date & time");
        colDateTime.setPrefWidth(100);
        colDateTime
                .setCellValueFactory(new Callback<TableColumn.CellDataFeatures<ObservableList, String>, ObservableValue<String>>() {
                    public ObservableValue<String> call(
                            TableColumn.CellDataFeatures<ObservableList, String> param) {
                                return new SimpleObjectProperty<String>(param
                                        .getValue().get(0).toString());
                            }
                });
        exceptionsTableViewColumns.add(colDateTime);

        TableColumn colLevel = new TableColumn("Level");
        colLevel.setPrefWidth(70);
        colLevel.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<ObservableList, String>, ObservableValue<String>>() {
            public ObservableValue<String> call(
                    TableColumn.CellDataFeatures<ObservableList, String> param) {
                return new SimpleStringProperty(param.getValue().get(1)
                        .toString());
            }
        });
        exceptionsTableViewColumns.add(colLevel);

        TableColumn colClass = new TableColumn("Class");
        colClass.setPrefWidth(200);
        colClass
                .setCellValueFactory(new Callback<TableColumn.CellDataFeatures<ObservableList, String>, ObservableValue<String>>() {
                    public ObservableValue<String> call(
                            TableColumn.CellDataFeatures<ObservableList, String> param) {
                                return new SimpleStringProperty(param.getValue().get(2)
                                        .toString());
                            }
                });
        exceptionsTableViewColumns.add(colClass);

        TableColumn colMessage = new TableColumn("Message");
        colMessage.setPrefWidth(exceptionsTableView.getWidth() - 370);
        colMessage
                .setCellValueFactory(new Callback<TableColumn.CellDataFeatures<ObservableList, String>, ObservableValue<String>>() {
                    public ObservableValue<String> call(
                            TableColumn.CellDataFeatures<ObservableList, String> param) {
                                return new SimpleStringProperty(param.getValue().get(3)
                                        .toString());
                            }
                });
        exceptionsTableViewColumns.add(colMessage);

        exceptionsTableView.getColumns().setAll(exceptionsTableViewColumns);
    }

    @SuppressWarnings("unchecked")
    public void buildQueriesTableView() {
        queriesTableViewColumns = FXCollections.observableArrayList();

        TableColumn colQuery = new TableColumn("Query");
        colQuery.setPrefWidth(queriesTableView.getWidth() - 300);
        colQuery.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<ObservableList, String>, ObservableValue<String>>() {
            public ObservableValue<String> call(
                    TableColumn.CellDataFeatures<ObservableList, String> param) {
                return new SimpleStringProperty(param.getValue().get(0)
                        .toString());
            }
        });
        queriesTableViewColumns.add(colQuery);

        TableColumn colTables = new TableColumn("Conserned tables");
        colTables.setPrefWidth(200);
        colTables
                .setCellValueFactory(new Callback<TableColumn.CellDataFeatures<ObservableList, String>, ObservableValue<String>>() {
                    public ObservableValue<String> call(
                            TableColumn.CellDataFeatures<ObservableList, String> param) {
                                return new SimpleStringProperty(param.getValue().get(1)
                                        .toString());
                            }
                });
        queriesTableViewColumns.add(colTables);

        TableColumn colExecutionTime = new TableColumn("Execution time(ms)");
        colExecutionTime.setPrefWidth(100);
        colExecutionTime
                .setCellValueFactory(new Callback<TableColumn.CellDataFeatures<ObservableList, Integer>, ObservableValue<Integer>>() {
                    public ObservableValue<Integer> call(
                            TableColumn.CellDataFeatures<ObservableList, Integer> param) {
                                ObservableValue<Integer> obsInt = new ReadOnlyObjectWrapper<Integer>(
                                        Integer.parseInt(param.getValue().get(2)
                                                .toString()));
                                return obsInt;
                            }
                });
        queriesTableViewColumns.add(colExecutionTime);
        queriesTableView.getColumns().setAll(queriesTableViewColumns);

    }

    public void reloadLogDirectoryTreeView() {
        buildLogDirectoryTreeView();
    }

    @SuppressWarnings("deprecation")
    public void buildLogDirectoryTreeView() {
        if (logDirectoryPath != null) {

            File ibmLogsLocationDirectory = new File(logDirectoryPath);// +
            // "\\WebSphere\\AppServer\\profiles\\ctgAppSrv01\\logs\\"
            if (ibmLogsLocationDirectory.exists()) {
                logDirectoryPath = ibmLogsLocationDirectory.getAbsolutePath();
            } else {
                Dialogs.create().owner(null).title("Error occured")
                        .masthead("IBM installation folder location")
                        .message("We cant locate IBM installation folder")
                        .showError();
            }

            rootItem = new TreeItem<String>(logDirectoryPath);
            rootItem.setExpanded(true);

            mxFolders = new ArrayList<File>();
            mxItems = new ArrayList<TreeItem<String>>();
            for (File mxFolderFile : new File(logDirectoryPath).listFiles()) {
                if (mxFolderFile.getName().startsWith("MXServer") && mxFolderFile.isDirectory()) {
                    mxFolders.add(mxFolderFile);
                    TreeItem<String> mxItem = new TreeItem<String>(mxFolderFile.getName());
                    mxItem.setExpanded(true);
                    mxItems.add(mxItem);

                    for (File logFile : mxFolderFile.listFiles()) {
                        if (logFile.getName().contains("System") && logFile.isFile() && logFile.getName().endsWith(".log")) {
                            mxItem.getChildren().add(new TreeItem<String>(logFile.getName()));
                        }
                    }
                    rootItem.getChildren().add(mxItem);
                }
            }
            logDirectory.setRoot(rootItem);
        }
    }

    public void locateLogDirectory() {
        String ibmLocation = "";
        for (File root : File.listRoots()) {
            ibmLocation += root.getPath();
            try {
                for (File dir : root.listFiles()) {
                    if (dir.getName().contains("IBM")
                            || dir.getName().contains("ibm")) {// C:\IBM
                        ibmLocation += dir.getName();
                        ibmLocationPath = ibmLocation;
                        logDirectoryPath = ibmLocation
                                + "\\WebSphere\\AppServer\\profiles\\ctgAppSrv01\\logs\\";
                        return;
                    } else if (dir.getName().contains("Program Files")) {// C:\Program
                        // Files\IBM
                        try {
                            for (File installDir : dir.listFiles()) {
                                if (installDir.getName().contains("IBM")
                                        || installDir.getName().contains("ibm")) {
                                    ibmLocation = installDir.getParent() + "\\"
                                            + installDir.getName();
                                    ibmLocationPath = ibmLocation;
                                    logDirectoryPath = ibmLocation
                                            + "\\WebSphere\\AppServer\\profiles\\ctgAppSrv01\\logs\\";
                                    return;
                                }
                            }
                        } catch (NullPointerException e) {
                            e.printStackTrace();
                        }
                    } else if (dir.getName().contains("Programmmes")) {// C:\Programmes\IBM
                        try {
                            for (File installDir : dir.listFiles()) {
                                if (installDir.getName().contains("IBM")
                                        || installDir.getName().contains("ibm")) {
                                    ibmLocation = installDir.getParent() + "\\"
                                            + installDir.getName();
                                    ibmLocationPath = ibmLocation;
                                    logDirectoryPath = ibmLocation
                                            + "\\WebSphere\\AppServer\\profiles\\ctgAppSrv01\\logs\\";
                                    return;
                                }
                            }
                        } catch (NullPointerException e) {
                            e.printStackTrace();
                        }
                    }
                }
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }
        if (!ibmLocation.contains("IBM")) {
            Dialogs.create().owner(null).title("Error occured")
                    .masthead("IBM installation folder location")
                    .message("We cannot locate IBM installation folder")
                    .showError();
            browseIBMLocationManuel();
        }
        ibmLocationPath = ibmLocation;
        logDirectoryPath = ibmLocation
                + "\\WebSphere\\AppServer\\profiles\\ctgAppSrv01\\logs\\";
        return;
    }

    @SuppressWarnings("deprecation")
    public void browseIBMLocationManuel() {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("IBM installation folder");
        File defaultDirectory = new File("C:/");
        chooser.setInitialDirectory(defaultDirectory);
        File selectedDirectory = chooser.showDialog(null);
        if (selectedDirectory != null && selectedDirectory.exists()) {
            ibmLocationPath = selectedDirectory.getAbsolutePath();
            logDirectoryPath = selectedDirectory.getAbsolutePath()
                    + "\\WebSphere\\AppServer\\profiles\\ctgAppSrv01\\logs\\";
            reloadLogDirectoryTreeView();
        }
    }

    public void browseLogLocationManuel() {
        logDirectory.getSelectionModel().clearSelection();
        logDirectory.getSelectionModel().select(-1);
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("IBM log folder");
        File defaultDirectory = new File("C:/");
        chooser.setInitialDirectory(defaultDirectory);
        File selectedDirectory = chooser.showDialog(null);
        if (selectedDirectory != null && selectedDirectory.exists()) {
            logDirectoryPath = selectedDirectory.getAbsolutePath();
            reloadLogDirectoryTreeView();
        }
        buildLogDirectoryTreeView();
    }

    @SuppressWarnings("unchecked")
    public void buildSearchInFilesResultsTableView() {
        ObservableList<TableColumn> searchInFilesTableViewColumns = FXCollections.observableArrayList();
        TableColumn colFileName = new TableColumn("File name");
        colFileName.setPrefWidth(200);
        colFileName
                .setCellValueFactory(new Callback<TableColumn.CellDataFeatures<ObservableList, String>, ObservableValue<String>>() {
                    public ObservableValue<String> call(
                            TableColumn.CellDataFeatures<ObservableList, String> param) {
                                return new SimpleObjectProperty<String>(param
                                        .getValue().get(0).toString());
                            }
                });
        searchInFilesTableViewColumns.add(colFileName);

        TableColumn colLineNumber = new TableColumn("Line number");
        colLineNumber.setPrefWidth(75);
        colLineNumber
                .setCellValueFactory(new Callback<TableColumn.CellDataFeatures<ObservableList, String>, ObservableValue<String>>() {
                    public ObservableValue<String> call(
                            TableColumn.CellDataFeatures<ObservableList, String> param) {
                                return new SimpleObjectProperty<String>(param
                                        .getValue().get(1).toString());
                            }
                });
        searchInFilesTableViewColumns.add(colLineNumber);

        TableColumn colLine = new TableColumn("Line");
        colLine.setPrefWidth(725);
        colLine
                .setCellValueFactory(new Callback<TableColumn.CellDataFeatures<ObservableList, String>, ObservableValue<String>>() {
                    public ObservableValue<String> call(
                            TableColumn.CellDataFeatures<ObservableList, String> param) {
                                return new SimpleObjectProperty<String>(param
                                        .getValue().get(2).toString());
                            }
                });
        searchInFilesTableViewColumns.add(colLine);
        searchInFilesTableView.getColumns().setAll(searchInFilesTableViewColumns);
    }
}
