import chess.*;
import com.google.gson.Gson;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.Array;
import java.util.*;

import datamodel.*;
import serverfacade.ServerFacade;
import ui.EscapeSequences;


public class Main {

    static ServerFacade facade = new ServerFacade(8080);

    public static void main(String[] args) throws Exception {
        System.out.println("♕ 240 Chess Client ♕");
        Scanner scanner = new Scanner(System.in);
        while (true) {
            var state = facade.getAuthToken() == null ? "LOGGED_OUT" : "LOGGED_IN";
            System.out.printf("[%s] >>> ", state);
            var inputWords = scanner.nextLine().split(" ");
            switch (inputWords[0].toLowerCase()) {
                case "clear": { // FOR TESTING ONLY, REMOVE BEFORE COMPLETION
                    facade.clear();
                    break;
                } case "help": {
                    if (state.equals("LOGGED_OUT")) {
                        System.out.println("""
                                \tregister <USERNAME> <EMAIL> <PASSWORD> | Register a new user
                                \tlogin <USERNAME> <PASSWORD> | Login to an existing user
                                \tquit | Quit the chess program
                                \thelp | See possible commands""");
                    } else {
                        System.out.println("""
                                \tcreate <GAME_NAME> | Create a new game
                                \tlist | List existing games
                                \tjoin <ID> <WHITE|BLACK> | Join a game
                                \tobserve <ID> | Observe a game
                                \tlogout | Logout when you are done
                                \tquit | Quit the chess program
                                \thelp | See possible commands""");
                    }
                    break;
                } case "quit": {
                    if (state.equals("LOGGED_IN")) {facade.logout();}
                    return;
                } case "login": {
                    facade.login(inputWords);
                    break;
                } case "register": {
                    facade.register(inputWords);
                    break;
                } case "logout": {
                    if (state.equals("LOGGED_OUT")) {
                        System.out.println("Logout utility not available while logged out");
                        break;
                    }
                    facade.logout();
                    break;
                } case "create": {
                    if (state.equals("LOGGED_OUT")) {
                        System.out.println("Create game utility not available while logged out");
                        break;
                    }
                    facade.create(inputWords);
                    break;
                } case "list": {
                    if (state.equals("LOGGED_OUT")) {
                        System.out.println("List games utility not available while logged out");
                        break;
                    }
                    facade.list();
                    break;
                } case "play": {
                    if (state.equals("LOGGED_OUT")) {
                        System.out.println("Play/join game utility not available while logged out");
                        break;
                    }
                    facade.join(inputWords);
                    break;
                } case "observe": {
                    if (state.equals("LOGGED_OUT")) {
                        System.out.println("Observe game utility not available while logged out");
                        break;
                    }
                    facade.observe(inputWords);
                    break;
                }
            }
        }
    }
}