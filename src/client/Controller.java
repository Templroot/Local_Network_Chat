package client;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import server.AuthService;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Controller {
    @FXML
    TextArea textArea;

    @FXML
    TextField textField;

    @FXML
    Button btn1;

    @FXML
    HBox bottomPanel;

    @FXML
    HBox upperPanel;

    @FXML
    HBox registredPanel;

    @FXML
    TextField loginField;

    @FXML
    TextField passwordField;

    @FXML
    TextField regLogin;

    @FXML
    PasswordField regPassword;

    @FXML
    TextField regNick;

    @FXML
    TextField regNumber;

    @FXML
    ListView<String> clientList;

    private boolean isAuthorized;

    // Проверка на авторизацию
    private void setAuthorized(boolean isAuthorized) {
        this.isAuthorized = isAuthorized;
        if (!isAuthorized) {
            upperPanel.setVisible(true);
            upperPanel.setManaged(true);
            registredPanel.setVisible(true);
            registredPanel.setManaged(true);
            bottomPanel.setVisible(false);
            bottomPanel.setManaged(false);
            clientList.setVisible(false);
            clientList.setManaged(false);
        } else {
            upperPanel.setVisible(false);
            upperPanel.setManaged(false);
            registredPanel.setVisible(false);
            registredPanel.setManaged(false);
            bottomPanel.setVisible(true);
            bottomPanel.setManaged(true);
            clientList.setVisible(true);
            clientList.setManaged(true);
        }
    }

    // клиентский сокет
    private Socket socket;
    // входящее сообщение
    private DataInputStream in;
    // исходящее сообщение
    private DataOutputStream out;

    // адрес сервера
    private final String IP_ADRESS = "localhost";
    // порт
    private final int PORT = 8189;

    private void connect() {
        // подключение к серверу
        try {
            socket = new Socket(IP_ADRESS, PORT);

            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());

            // в отдельном потоке начинаем работу с сервером
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        // бесконечный цикл
                        while (true) {
                            String str = in.readUTF();
                            // если есть входящее сообщение от
                            // авторизированного клиента
                            if (str.startsWith("/authok")) {
                                setAuthorized(true);
                                // логируем
                                loadHistory();
                                break;
                            } else {
                                textArea.appendText(str + "\n");
                            }
                        }

                        while (true) {
                            String str = in.readUTF();
                            if (str.equals("/serverclosed")) break;
                            if (str.startsWith("/clientlist")) {
                                String[] tokens = str.split(" ");
                                Platform.runLater(new Runnable() {
                                    @Override
                                    public void run() {
                                        clientList.getItems().clear();
                                        for (int i = 1; i < tokens.length; i++) {
                                            clientList.getItems().add(tokens[i]);
                                        }
                                    }
                                });
                            } else {
                                textArea.appendText(str + "\n");
                                SaveHistory();
                            }
                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            socket.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        setAuthorized(false);
                    }
                }
            }).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void Dispose() {
        // закрытие приложения
        System.out.println("Закрытие...");
        try {
            if (out != null) {
                out.writeUTF("/end");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMsg() {
        // отправка сообщения
        try {
            if (textField.getText().length() != 0) {
                out.writeUTF(textField.getText());
                textField.clear();
                textField.requestFocus();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void tryToAuth() {
        // авторизация
        if (socket == null || socket.isClosed()) {
            connect();

        }
        if (passwordField.getText().length() != 0 && loginField.getText().length() != 0) {
            int hash = passwordField.getText().hashCode();
            try {
                out.writeUTF("/auth " + loginField.getText() + " " + hash);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            loginField.clear();
            passwordField.clear();
            try {
                File file = new File("history.txt");
                //создаем объект FileReader для объекта File
                FileReader fr = new FileReader(file);
                //создаем BufferedReader с существующего FileReader для построчного считывания
                BufferedReader reader = new BufferedReader(fr);
                // считаем сначала первую строку
                while (reader.readLine() != null) {
                    // считываем остальные строки в цикле
                    textArea.appendText(reader.readLine() + "\n");
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

        } else {
            System.out.println("Вы ввели не все данные");
        }

    }

    public String validate_phone(String telephone) {
        // валидация телефона
        telephone = telephone.replaceAll(" ", "");

        if (telephone.length() >= 17 || telephone.length() < 11 || telephone.contains("[^A-Za-zА-Яа-я]")) {
            return "";
        }
        telephone = telephone.replaceAll("[-()]", "");
        if (telephone.charAt(0) == '+' && telephone.charAt(1) != '7') {
            return "";
        }
        System.out.println(telephone);
        if (telephone.charAt(0) == '8') {
            telephone = telephone.replaceFirst("8", "+7");
        }
        return telephone;

    }

    public void registration() {
        // регистрация

        if (socket == null || socket.isClosed()) {
            connect();
        }
        String phone = validate_phone(regNumber.getText());
        if (regPassword.getText().length() != 0 && regLogin.getText().length() != 0 && regNick.getText().length() != 0 && phone.length() != 0) {
            AuthService.setNewUsers(regLogin.getText(), regPassword.getText(), regNick.getText(), phone);
            System.out.println("Поздравляем с успешной регистрацией.\nВойдите через форму авторизации. ");

        } else {
            System.out.println("Вы ввели невернкые данные");
        }
        regLogin.clear();
        regPassword.clear();
        regNick.clear();
        regNumber.clear();


    }

    public void selectClient(MouseEvent mouseEvent) {
        // выбрать пользователя

        if (mouseEvent.getClickCount() == 2) {
            System.out.println("Двойной клик");
        }
    }

    private void SaveHistory() throws IOException {
        // сохранить инсторию
        try {
            File history = new File("history.txt");
            if (!history.exists()) {
                System.out.println("Файла истории нет,создадим его");
                history.createNewFile();
            }
            PrintWriter fileWriter = new PrintWriter(new FileWriter(history, true));

            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
            bufferedWriter.write(textArea.getText());
            bufferedWriter.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadHistory() throws IOException {
        // загрузить историю

        int posHistory = 0;
        File history = new File("history.txt");
        List<String> historyList = new ArrayList<>();
        FileInputStream in = new FileInputStream(history);
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in));

        String temp;
        while ((temp = bufferedReader.readLine()) != null) {
            historyList.add(temp);
        }

        if (historyList.size() > posHistory) {
            for (int i = historyList.size() - posHistory; i <= (historyList.size() - 1); i++) {
                textArea.appendText(historyList.get(i) + "\n");
            }
        } else {
            for (int i = 0; i < posHistory; i++) {
                System.out.println(historyList.get(i));
            }
        }

    }
}