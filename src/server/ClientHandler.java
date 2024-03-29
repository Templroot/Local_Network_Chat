package server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

class ClientHandler {
    // экземпляр нашего сервера
    private Socket socket;
    // входящее собщение
    private DataInputStream in;
    // входящее собщение
    private DataOutputStream out;
    private Main server;
    private String nick;

    String getNick() {
        return nick;
    }

    boolean checkBlackList(String _blacklist_nick) {
        // blacklist user - banned user
        int check_nick = AuthService.getIdByNick(_blacklist_nick);
        int my_id = AuthService.getIdByNick(nick);
        int blacklistId = AuthService.getBlackListUserById(my_id, check_nick);
        return blacklistId > 0;
    }

    public ClientHandler(Socket socket, Main server) {
        // конструктор, который принимает клиентский сокет и сервер
        try {
            this.socket = socket;
            this.server = server;
            this.in = new DataInputStream(socket.getInputStream());
            this.out = new DataOutputStream(socket.getOutputStream());

            // Переопределяем метод run(), который вызывается когда
            // мы вызываем new Thread(client).start();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        while (true) {
                            String str = in.readUTF();
                            if (str.startsWith("/auth")) {
                                String[] tokens = str.split(" ");
                                String newNick = AuthService.getNickByLoginAndPass(tokens[1], tokens[2]);
                                if (newNick != null) {
                                    if (!server.isNickBusy(newNick)) {
                                        sendMsg("/authok");
                                        nick = newNick;
                                        server.subscribe(ClientHandler.this);
                                        break;
                                    } else {
                                        sendMsg("Учетная запись уже используется");
                                    }
                                } else {
                                    sendMsg("Неверный логин/пароль");
                                }
                            }
                            server.broadcastMsg(ClientHandler.this, str);
                        }
                        // Если от клиента пришло сообщение
                        while (true) {
                            String str = in.readUTF();
                            if (str.startsWith("/")) {
                                if (str.equals("/end")) {
                                    out.writeUTF("/serverclosed");
                                    break;
                                }
                                if (str.startsWith("/w ")) {
                                    String[] tokens = str.split(" ", 3);
                                    server.sendPersonalMsg(ClientHandler.this, tokens[1], tokens[2]);
                                }
                                if (str.startsWith("/blacklist ")) {
                                    String[] users_to_ban = str.split(" ");
                                    int nickId = AuthService.getIdByNick(nick);
                                    int nicknameId = AuthService.getIdByNick(users_to_ban[1]);
                                    AuthService.addBlackListByNickAndNickName(nickId, nicknameId);
                                    sendMsg("Вы добавили пользователя " + users_to_ban[1] + " в черный список");
                                }
                            } else {
                                server.broadcastMsg(ClientHandler.this, nick + ": " + str);
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            in.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        try {
                            out.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        try {
                            socket.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    server.unsubscribe(ClientHandler.this);
                }
            }).start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // отправляем сообщение
    void sendMsg(String msg) {
        try {
            out.writeUTF(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}