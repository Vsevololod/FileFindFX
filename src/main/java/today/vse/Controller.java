package today.vse;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Controller {

    public TabPane mainTabPane;
    private ConcurrentLinkedQueue<Path> filePatchQueue = new ConcurrentLinkedQueue<>();
    private Path rootPath = Paths.get(System.getProperty("user.dir"));
    private TreeItem<Path> rootTreeItem = new TreeItem<>(rootPath);

    @FXML
    public TextField searchText;

    @FXML
    public TextField searchPattern;

    @FXML
    public Button searchButton;

    @FXML
    public TreeView<Path> mainTreeView;

    @FXML
    private Button buttonChooseRoot;

    @FXML
    private TextField rootPathField;

    private TreeItem<Path> findByValue(List<TreeItem<Path>> list, Object value) {
        for (TreeItem<Path> p : list) {
            if (p.getValue().equals(value)) {
                return p;
            }
        }
        return null;
    }

    private void updateRootPath(Path path) {
        rootPath = path;
        rootPathField.setText(path.toString());
        rootTreeItem.setValue(path);
    }


    private void update() {
        while (!filePatchQueue.isEmpty()) {
            Path temp = filePatchQueue.poll();
            Path subPath = temp.subpath(rootPath.getNameCount(), temp.getNameCount());
            TreeItem<Path> tempTreeItem = rootTreeItem;

            for (Path path : subPath) {
                ObservableList<TreeItem<Path>> children = tempTreeItem.getChildren();
                TreeItem<Path> targetNode = findByValue(children, path);

                if (targetNode != null) {
                    tempTreeItem = targetNode;
                } else {
                    TreeItem<Path> newPath = new TreeItem<>(path);
                    children.add(newPath);
                    tempTreeItem = newPath;
                }
                children.sort(Comparator.comparing(TreeItem::getValue));
            }
        }
    }


    private void setListeners() {

        searchButton.setOnMouseClicked(mouseEvent -> {
            FileSearchThread fileSearch = new FileSearchThread(rootPath, searchPattern.getText(), searchText.getText(), filePatchQueue);
            fileSearch.run();
            try {
                fileSearch.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

        buttonChooseRoot.addEventHandler(MouseEvent.MOUSE_CLICKED, mouseEvent -> {
            Stage showDialog = new Stage();
            DirectoryChooser directoryChooser = new DirectoryChooser();
            directoryChooser.setTitle("Open Resource File");
            File selectedFile = directoryChooser.showDialog(showDialog);
            if (selectedFile != null) {
                updateRootPath(selectedFile.toPath());

            }
        });

        mainTreeView.setOnMouseClicked(mouseEvent -> {
            if (mouseEvent.getClickCount() == 2) {
                TreeItem<Path> item = mainTreeView.getSelectionModel().getSelectedItem();
                Path file = item.getValue();

                for (TreeItem<Path> it = item.getParent(); it != null; it = it.getParent()) {
                    file = Paths.get(it.getValue().toString(), file.toString());
                }
                mainTabPane.getTabs().add(new FileTab(file).getTab());
            }
        });

        rootPathField.focusedProperty().addListener((observableValue, aBoolean, t1) -> {
            if (!t1) {
                updateRootPath(Paths.get(rootPathField.getText()));
            }
        });
    }


    @FXML
    public void initialize() {
        Timeline fiveSecondsWonder = new Timeline(new KeyFrame(Duration.seconds(5), event -> update()));
        fiveSecondsWonder.setCycleCount(Timeline.INDEFINITE);
        fiveSecondsWonder.play();

        rootPathField.setText(rootPath.toString());
        mainTreeView.setRoot(rootTreeItem);

        setListeners();

    }
}
