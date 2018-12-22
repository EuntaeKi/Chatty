import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smack.roster.RosterEntry;
import org.jivesoftware.smack.roster.RosterGroup;
import org.jivesoftware.smackx.vcardtemp.VCardManager;
import org.jivesoftware.smackx.vcardtemp.packet.VCard;
import org.jxmpp.jid.EntityBareJid;
import org.jxmpp.jid.impl.JidCreate;

import java.util.Collection;

public class Controller {

    @FXML private RadioButton friendListBtn;
    @FXML private RadioButton chatListBtn;
    @FXML private RadioButton settingBtn;
    @FXML private BorderPane friendListPane;
    @FXML private BorderPane chatListPane;
    @FXML private BorderPane settingPane;
    @FXML private StackPane outline;
    @FXML private Button closeBtn;
    @FXML private Button miniBtn;
    @FXML private Button restoreBtn;
    @FXML private ToolBar windowBar;
    @FXML private Button loginBtn;
    @FXML private BorderPane loginPane;
    @FXML private BorderPane backbone;
    @FXML private VBox loginFailBox;
    @FXML private TextField idField;
    @FXML private PasswordField pwField;
    @FXML private Label loginFail;
    @FXML private Label label;
    @FXML private VBox friendBox;
    @FXML private BorderPane root;

    private ChattyXMPPConnection client;
    private VCardManager vcard;

    public void initialize() {
        // Log in UI
        loginPane.toFront();
        idField.setOnKeyPressed((KeyEvent e) -> {
            if (e.getCode().equals(KeyCode.ENTER)) {
                login();
            }
        });
        pwField.setOnKeyPressed((KeyEvent e) -> {
            if (e.getCode().equals(KeyCode.ENTER)) {
                login();
            }
        });
        loginBtn.setOnMousePressed((MouseEvent e) -> {
            System.out.println("Click");
            if (e.getButton().equals(MouseButton.PRIMARY)) {
                System.out.println("Left");
                login();
            }
        });

        // EventHandler for buttons to switch panel on StackPane
        // Search button might be added later
        friendListBtn.setOnMousePressed((MouseEvent e) -> {
            friendListPane.toFront();
            System.out.println("friend");
        });
        chatListBtn.setOnMousePressed((MouseEvent e) -> {
            chatListPane.toFront();
            System.out.println("chat");
        });
        settingBtn.setOnMousePressed((MouseEvent e) -> {
            settingPane.toFront();
            System.out.println("setting");
        });

        // Closes the window once clicked
        closeBtn.setOnMousePressed((MouseEvent e) -> {
            Platform.exit();
        });

        // Minimizes the window once clicked
        miniBtn.setOnMousePressed((MouseEvent e) -> {
            Main.primaryStage.setIconified(true);
        });

        // Max & minimizes the window once clicked
        restoreBtn.setOnMousePressed((MouseEvent e) -> {
            if (Main.primaryStage.isMaximized()) {
                Main.primaryStage.setMaximized(false);
            } else {
                Main.primaryStage.setMaximized(true);
            }
        });

        root.setOnKeyPressed((KeyEvent e) -> {
            System.out.println("Pressed");
            if (e.getCode().equals(KeyCode.ESCAPE)) {
                Platform.exit();
            }
        });

        // Change in coordinate saved in a vector form
        class Delta {
            double x, y;
        }
        // Allows the window to move around based on the mouse coordinate
        final Delta dragDelta = new Delta();
        windowBar.setOnMousePressed((MouseEvent e) -> {
            dragDelta.x = Main.primaryStage.getX() - e.getScreenX();
            dragDelta.y = Main.primaryStage.getY() - e.getScreenY();
        });
        windowBar.setOnMouseDragged((MouseEvent e) -> {
            Main.primaryStage.setX(e.getScreenX() + dragDelta.x);
            Main.primaryStage.setY(e.getScreenY() + dragDelta.y);
        });

    }

    // Methods for loading the friend screen
    // Gets their nickname if it exists
    // Lists all the friends they have
    private void friendLoad() throws Exception {
        Separator y = new Separator();
        friendBox.getChildren().add(y);
        for (RosterEntry friend : client.getFriend()) {
            String displayName;
            vcard = VCardManager.getInstanceFor(client.connection);
            EntityBareJid specialJid = JidCreate.entityBareFrom(friend.getJid());
            if (vcard.loadVCard(specialJid).getNickName() != null) {
                displayName = vcard.loadVCard().getNickName();
            } else {
                displayName = friend.getName();
            }
            HBox friendHBox = new HBox();
            ToggleButton friendBtn = new ToggleButton(displayName);
            friendBtn.setTextFill(Color.web("#7f7f7f"));
            friendBtn.setStyle("-fx-font-weight: bold");
            friendBtn.setOnMousePressed((MouseEvent e)-> {
                makeFriendPopup(displayName, specialJid);
            });
            friendHBox.setPadding(new Insets(5, 5, 5, 10));
            friendHBox.getChildren().add(friendBtn);
            friendBox.getChildren().add(friendHBox);
            System.out.println("Friend Loaded");
        }
    }

    // Log in the user
    private void login() {
        try {
            client = new ChattyXMPPConnection(idField.getText(), pwField.getText());
            System.out.println("User Data Generated");
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            client.connection.connect();
            System.out.println("Server Connected");
        } catch (Exception e) {
            loginFailBox.getChildren().add(new Text("Server is offline."));
            throw new Error("Cannot connect to the server");
        }
        try {
            client.connection.login();
            System.out.println("User Logged in");
            idField.getStyleClass().remove("fail");
            pwField.getStyleClass().remove("fail");
            backbone.toFront();
            friendListBtn.setSelected(true);
            friendListPane.toFront();
            try {
                friendLoad();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            idField.getStyleClass().add("fail");
            pwField.getStyleClass().add("fail");
            loginFail.setText("Log in has failed. Check your ID or your password.");
            throw new Error("Log in has failed");
        }
    }

    public void makeFriendPopup (String displayName, EntityBareJid specialJid) {
        GridPane popupPane = new GridPane();
        popupPane.setBackground(new Background(new BackgroundFill(Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY)));
        Label name = new Label(displayName);
        name.setPadding(new Insets(5));
        TextField nameChange = new TextField();
        nameChange.setVisible(false);
        Button unClickedBtn = new Button("Set Nickname");
        Button saveBtn = new Button("Save");
        saveBtn.setVisible(false);
        popupPane.add(name, 1, 0, 1, 1);
        popupPane.add(saveBtn, 2, 0, 1, 1);
        popupPane.add(unClickedBtn, 2, 0, 1 ,1);
        popupPane.add(nameChange, 1, 0, 1, 1);
        final Stage friendWindow = new Stage();
        friendWindow.initModality(Modality.NONE);
        Scene windowScene = new Scene(popupPane, 150, 200);
        friendWindow.setScene(windowScene);
        friendWindow.initStyle(StageStyle.UNDECORATED);
        root.setOnMousePressed((MouseEvent cursor)->{
            friendWindow.hide();
        });
        friendWindow.show();
        unClickedBtn.setOnMousePressed((MouseEvent click)-> {
            name.setVisible(false);
            nameChange.setVisible(true);
            unClickedBtn.setVisible(false);
            saveBtn.setVisible(true);
        });
        saveBtn.setOnMousePressed((MouseEvent click)-> {
            try {
                VCard loaded = vcard.loadVCard(specialJid);
                System.out.println(loaded);
                loaded.setNickName(nameChange.getText());
                vcard.saveVCard(loaded);
                name.setVisible(true);
                nameChange.setText("");
                nameChange.setVisible(false);
                unClickedBtn.setVisible(true);
                saveBtn.setVisible(false);
                friendBox.getChildren().removeAll(friendBox.getChildren());
                friendLoad();
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        });
    }
}
