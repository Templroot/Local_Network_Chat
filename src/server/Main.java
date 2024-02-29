package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;

public class Main {
    private Vector<ClientHandler> clients;

    public Main() {
        clients = new Vector<>();
        // сокет клиента, это некий поток, который будет подключаться к серверу
        // по адресу и порту
        ServerSocket server = null;
        // серверный сокет
        Socket socket = null;

        try {
            AuthService.connection();
            // создаём серверный сокет на определенном порту
            server = new ServerSocket(8189);
            System.out.println("Сервер запущен");

            // запускаем бесконечный цикл
            while (true) {
                // Ждём подключений от сервера
                socket = server.accept();
                System.out.println("Клиент подключился");
                // создаём обработчик клиента, который подключился к серверу
                // this - это наш сервер
                new ClientHandler(socket, this);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    boolean isNickBusy(String nick) {
        // проверка на уникальность ника
        for (ClientHandler o : clients) {
            if (o.getNick().equals(nick)) {
                return true;
            }
        }
        return false;
    }

    void subscribe(ClientHandler client) {
        clients.add(client);
        broadcastClientList();
    }

    void unsubscribe(ClientHandler client) {
        clients.remove(client);
        broadcastClientList();
    }

    void broadcastMsg(ClientHandler from, String msg) {
        for (ClientHandler o : clients) {
            if (!o.checkBlackList(from.getNick())) {
                o.sendMsg(msg);
            }
        }
    }

    void sendPersonalMsg(ClientHandler from, String nickTo, String msg) {
        boolean banned = false;
        for (ClientHandler o : clients) {
            if (o.getNick().equals(nickTo)) {
                if (!o.checkBlackList(from.getNick())) {
                    o.sendMsg("from " + from.getNick() + ": " + msg);
                    from.sendMsg("to " + nickTo + ": " + msg);
                    return;
                }
                banned = true;
                from.sendMsg("Этот пользователь вас забанил(");
                return;
            }
        }
        if (!banned) {
            from.sendMsg("Клиент с ником " + nickTo + " не найден в чате");

        }


    }

    private void broadcastClientList() {
        StringBuilder sb = new StringBuilder();
        sb.append("/clientlist ");
        for (ClientHandler o : clients) {
            sb.append(o.getNick() + " ");
        }

        String out = sb.toString();

        for (ClientHandler o : clients) {
            o.sendMsg(out);
        }
    }
}