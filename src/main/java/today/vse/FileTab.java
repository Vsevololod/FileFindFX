package today.vse;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ListView;
import javafx.scene.control.Tab;

import java.io.*;
import java.nio.file.Path;
import java.util.LinkedList;

class FileTab {
    private BufferedReader fileReader;
    private ListView<String> listView;
    private Tab tab;
    private String getNextString = "click to get next =>";

    private void addLines() {
        int i = 250;
        listView.getItems().remove(getNextString);
        String line;
        try {
            while ((line = fileReader.readLine()) != null && i-- > 0) {
                listView.getItems().add(line);
            }
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
        try {
            if (fileReader.ready()) {
                listView.getItems().add(getNextString);
            }
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

    FileTab(Path file) {
        try {
            fileReader = new BufferedReader(new FileReader(file.toFile()));
        } catch (FileNotFoundException e) {
            fileReader = new BufferedReader(new StringReader(e.toString()));
        }

        ObservableList<String> list = FXCollections.observableList(new LinkedList<>());

        listView = new ListView<>(list);
        listView.setOnMouseClicked(mouseEvent -> {
            for (String s : listView.getSelectionModel().getSelectedItems()) {
                if (s.equals(getNextString)) {
                    addLines();
                }
            }
        });

        tab = new Tab();
        tab.setText(file.getFileName().toString());
        tab.setContent(listView);

        addLines();
    }

    Tab getTab() {
        return tab;
    }
}
