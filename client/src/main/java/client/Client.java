package client;

public interface Client {

    public void printPrompt();

    public String eval(String command)  throws Exception;
}
