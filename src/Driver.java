import javax.swing.JFrame; // import used for setting up the main GUI
import javax.swing.JOptionPane; // import used for GUI prompting

import java.io.*; // import used for reading and writing website responses/requests
import java.net.*; // import used for sending HTTP GET and POST requests to the server

import java.util.regex.*; // import used to check if username and password are alphanumeric

/**
 * The Driver class is responsible for starting up a JPanel with the Screen class and the login functionality.
 * The user is first prompted with three options to start with:
 *      1. Create a new user account
 *      2. Login with an existing account
 *      3. Quit the program
 *
 * ### OPTION 1 - Sign Up ###
 * If the user decides to select the "Sign Up" button (create a new user account), the program will prompt for a username and password.
 * The program will then send a POST request to the web server and have it update the database so the user can log in the next time.
 * After the database is updated, the user is logged in.
 * If the server has an internal error, the program will notify the user that they cannot create a user at the moment and will have to wait till later.
 *
 * ### OPTION 2 - Login ###
 * If the user decides to select the "Login" button (log in with an existing account), the program will prompt for the user's username and password.
 * The program will then send a GET request to the web server and receive information that will determine if the set of credentials is legitimate.
 * If the set of credentials does not match anything from the database, the user will be notified of the incorrect credentials and redirected to the first prompt.
 * If the server has an internal error, the program will notify the user that they cannot log in at the moment and will have to wait till later.
 *
 * ### OPTION 3 - Quit ###
 * If the user decides to select the "Quit" button, the program will close itself.
 * <br>
 * ---OTHER INFORMATION---<br>
 * @version 1.1
 * @since   2019-03-10
 */
public class Driver {
    private static final String USER_AGENT = "Mozilla/5.0"; // the agent on which to send GET and POST requests
    private static final String BASE_URL = "http://X.X.X.X/"; // the base URL of the server
    private static final String VERSION = "1.1"; // the version of the program
    private static final String FORUM_NAME = "Fileforum"; // the name of the forums

    private static JFrame frame; // the frame of the main program

    /**
     * Main method that is the first to run to start up the program.
     * When creating a user or logging in, it will check if the supplied username and password are not empty and only contain alphanumerical characters. This is to prevent certain attacks upon the server.
     * @param args the arguments supplied by the user for modification to the program
     */
    public static void main(String[] args) {
        String getResult = "";
        String username = "";
        String password = "";
        Object[] options = {"Sign Up", "Login", "Quit"};
        // loop prompting until successful creation of user, successful login, or termination of program
        do {
            int create = JOptionPane.showOptionDialog(null, "", FORUM_NAME + " Login Page", JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[1]);
            switch (create) {
                case 0: // create user
                    username = JOptionPane.showInputDialog("Enter a username", username); // get username from user
                    password = JOptionPane.showInputDialog("Enter a password"); // get password from user
                    if (username.isEmpty() || !isValid(username) || password.isEmpty() || !isValid(password)) {
                        JOptionPane.showMessageDialog(null, "Username and password cannot be blank and can only include alphanumeric or any of these characters:\n'!', '.', ',', '$', '+', '-', '*', '(', ')', '_'\nTry again.", "Illegal Characters", JOptionPane.ERROR_MESSAGE);
                        break;
                    }
                    try {
                        addUser(username,password);
                        getResult = getUser(username, password);
                    } catch (ConnectException e) {
                        JOptionPane.showMessageDialog(null, "Cannot contact the server. Try again later.", "Server Error", JOptionPane.ERROR_MESSAGE);
                    } catch (Exception e) {
                        JOptionPane.showMessageDialog(null, "Username already exists.", "Sign In Error", JOptionPane.ERROR_MESSAGE);
                    }
                    break;
                case 1: // login
                    username = JOptionPane.showInputDialog("Enter your username", username); // get username from user
                    password = JOptionPane.showInputDialog("Enter your password"); // get password from user
                    if (username.isEmpty() || !isValid(username) || password.isEmpty() || !isValid(password)) {
                        JOptionPane.showMessageDialog(null, "Username and password cannot be blank and can only include alphanumeric or any of these characters:\n'!', '.', ',', '$', '+', '-', '*', '(', ')', '_'\nTry again.", "Illegal Characters", JOptionPane.ERROR_MESSAGE);
                        break;
                    }
                    try {
                        getResult = getUser(username, password);
                        if (getResult.equals("")) throw new Exception("e");
                    } catch (ConnectException e) {
                        JOptionPane.showMessageDialog(null, "Cannot contact the server. Try again later.", "Server Error", JOptionPane.ERROR_MESSAGE);
                    } catch (Exception e) {
                        JOptionPane.showMessageDialog(null, "Username and password combination does not exist. Try again.", "Credentials Invalid", JOptionPane.ERROR_MESSAGE);
                        getResult = "";
                    }
                    break;
                default: // quit
                    System.exit(0);
            }
        } while (getResult.equals(""));
        // notify user that they are successfully logged in
        JOptionPane.showMessageDialog(null, "You are logged in, " + username + ". Welcome to " + FORUM_NAME + "!", "Successful Login", JOptionPane.INFORMATION_MESSAGE);
        // create and set up the main GUI of the program
        frame = new JFrame(FORUM_NAME + " - Version " + VERSION);
        frame.setSize(600, 800);
        frame.setLocation(400, 200);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setContentPane(new Screen(username, USER_AGENT, BASE_URL, FORUM_NAME));
        frame.setVisible(true);
    }

    /**
     * Disposes the frame (main GUI of the program) whenever the user decides to log off.
     * It will notify the user that they logged off, then it will redirect them to the beginning prompt.
     */
    public static void reboot() {
        frame.dispose();
        JOptionPane.showMessageDialog(null, "You have logged out. See you soon!", "Logged Out", JOptionPane.INFORMATION_MESSAGE);
        main(new String[] {""});
    }

    /**
     * Sends a GET request to prompt whether a user exists.
     * If there is a connection error, a ConnectException will be thrown.
     * @param username the username of the user logging in
     * @param password the password of the user logging in
     * @return the result of the GET request
     * @throws Exception an error in retrieval of data, either client or server side
     */
    private static String getUser(String username, String password) throws Exception {
        StringBuilder result = new StringBuilder();
        URL url = new URL(BASE_URL + "posts.txt");
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");
        con.setRequestProperty("User-Agent", USER_AGENT);
        int responseCode = con.getResponseCode();
        if (responseCode != 200) {
            throw new ConnectException("Cannot contact server");
        }
        url = new URL(BASE_URL + "users/" + username + "/" + password + ".txt");
        con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");
        con.setRequestProperty("User-Agent", USER_AGENT);
        responseCode = con.getResponseCode();
        if (responseCode == 200) {
            BufferedReader rd = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String line;
            while ((line = rd.readLine()) != null) {
                result.append(line);
            }
            rd.close();
            return result.toString();
        }
        return null;
    }

    /**
     * Sends a POST request to create a new user.
     * If there is a connection error, a ConnectException will be thrown.
     * @param username the username of the user to create
     * @param password the password of the user to create
     * @throws Exception an error in sending data, typically due to server internal issues
     */
    private static void addUser(String username, String password) throws Exception {
        URL url = new URL(BASE_URL + "posts.txt");
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");
        con.setRequestProperty("User-Agent", USER_AGENT);
        int responseCode = con.getResponseCode();
        if (responseCode != 200) {
            throw new ConnectException("Cannot contact server");
        }
        url = new URL(BASE_URL + "api/addUser.php");
        con = (HttpURLConnection)url.openConnection();
        con.setRequestMethod("POST");
        con.setRequestProperty("User-Agent", USER_AGENT);
        con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
        String urlParameters = "username=" + username + "&password=" + password;
        con.setDoOutput(true);
        DataOutputStream wr = new DataOutputStream(con.getOutputStream());
        wr.writeBytes(urlParameters);
        wr.flush();
        wr.close();
        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();
        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
    }

    /**
     * Checks if the given parameter is valid for a password
     * @param s the String to check
     * @return true if allowed as a password, false if not '!', '.', ',', '$', '+', '-', '*', '(', ')', '_'
     */
    private static boolean isValid(String s) {
        Pattern p = Pattern.compile("[a-zA-Z0-9!.,$+\\-*()_]+");
        Matcher m = p.matcher(s);
        return m.matches();
    }

    /**
     * Custom exception thrown when response code of GET/POST requests is not 200, indicating connection error.
     */
    private static class ConnectException extends Exception {
        public ConnectException(String s) {
            super(s);
        }
    }
}
