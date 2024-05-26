import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class LibraryManagementSystem extends JFrame {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/library";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "0172samasrat";

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("d/M/yyyy");
    private static Connection connection;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(LibraryManagementSystem::new);
    }

    public LibraryManagementSystem() {
        connectToDatabase();
        createAndShowGUI();
    }

    private void createAndShowGUI() {
        setTitle("Library Management System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 400);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new GridLayout(5, 1, 10, 10));
        mainPanel.setBackground(Color.LIGHT_GRAY);

        JButton addBookButton = new JButton("Add Book");
        JButton searchBookButton = new JButton("Search Book");
        JButton viewBooksButton = new JButton("View Books");
        JButton deleteBookButton = new JButton("Delete Book");
        JButton exitButton = new JButton("Exit");

        addBookButton.addActionListener(e -> addBook());
        searchBookButton.addActionListener(e -> searchBooks());
        viewBooksButton.addActionListener(e -> viewBooks());
        deleteBookButton.addActionListener(e -> deleteBooks());
        exitButton.addActionListener(e -> System.exit(0));

        mainPanel.add(addBookButton);
        mainPanel.add(searchBookButton);
        mainPanel.add(viewBooksButton);
        mainPanel.add(deleteBookButton);
        mainPanel.add(exitButton);

        add(mainPanel);
        setVisible(true);
    }

    private static void connectToDatabase() {
        try {
            connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            System.out.println("Connected to database.");
        } catch (SQLException e) {
            System.err.println("Error connecting to the database: " + e.getMessage());
            JOptionPane.showMessageDialog(null, "Error connecting to the database. Please check your connection settings.", "Database Connection Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void addBook() {
        if (connection == null) {
            JOptionPane.showMessageDialog(this, "Not connected to the database.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JDialog addBookDialog = new JDialog(this, "Add Book", true);
        addBookDialog.setLayout(new GridLayout(7, 2, 10, 10));
        addBookDialog.setSize(400, 300);
        addBookDialog.setLocationRelativeTo(this);

        JTextField bookIdField = new JTextField();
        JTextField bookNameField = new JTextField();
        JTextField authorNameField = new JTextField();
        JTextField studentNameField = new JTextField();
        JTextField studentAddrField = new JTextField();
        JTextField bookIssueDateField = new JTextField(LocalDate.now().format(DATE_FORMAT));

        addBookDialog.add(new JLabel("Book ID:"));
        addBookDialog.add(bookIdField);
        addBookDialog.add(new JLabel("Book Name:"));
        addBookDialog.add(bookNameField);
        addBookDialog.add(new JLabel("Author Name:"));
        addBookDialog.add(authorNameField);
        addBookDialog.add(new JLabel("Student Name:"));
        addBookDialog.add(studentNameField);
        addBookDialog.add(new JLabel("Student Address:"));
        addBookDialog.add(studentAddrField);
        addBookDialog.add(new JLabel("Book Issue Date:"));
        addBookDialog.add(bookIssueDateField);

        JButton submitButton = new JButton("Submit");
        submitButton.addActionListener(e -> {
            try {
                int bookId = Integer.parseInt(bookIdField.getText().trim());
                String bookName = bookNameField.getText().trim();
                String authorName = authorNameField.getText().trim();
                String studentName = studentNameField.getText().trim();
                String studentAddr = studentAddrField.getText().trim();
                LocalDate bookIssueDate = LocalDate.parse(bookIssueDateField.getText().trim(), DATE_FORMAT);
                Date sqlDate = Date.valueOf(bookIssueDate);

                if (bookName.isEmpty() || authorName.isEmpty() || studentName.isEmpty() || studentAddr.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Please fill in all fields.", "Input Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                String sql = "INSERT INTO books (bookId, bookName, authorName, studentName, studentAddr, bookIssueDate) VALUES (?, ?, ?, ?, ?, ?)";
                try (PreparedStatement ps = connection.prepareStatement(sql)) {
                    ps.setInt(1, bookId);
                    ps.setString(2, bookName);
                    ps.setString(3, authorName);
                    ps.setString(4, studentName);
                    ps.setString(5, studentAddr);
                    ps.setDate(6, sqlDate);
                    ps.executeUpdate();
                    JOptionPane.showMessageDialog(this, "Book added successfully!");
                    addBookDialog.dispose();
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Book ID must be a number.", "Input Error", JOptionPane.ERROR_MESSAGE);
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        addBookDialog.add(submitButton);
        addBookDialog.setVisible(true);
    }

    private void searchBooks() {
        if (connection == null) {
            JOptionPane.showMessageDialog(this, "Not connected to the database.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String bookIdStr = JOptionPane.showInputDialog(this, "Enter Book ID:");
        if (bookIdStr != null && !bookIdStr.trim().isEmpty()) {
            try {
                int bookId = Integer.parseInt(bookIdStr.trim());
                String sql = "SELECT * FROM books WHERE bookId = ?";
                try (PreparedStatement ps = connection.prepareStatement(sql)) {
                    ps.setInt(1, bookId);
                    ResultSet rs = ps.executeQuery();
                    if (rs.next()) {
                        String bookDetails = "Book ID: " + rs.getInt("bookId") + "\n" +
                                "Book Name: " + rs.getString("bookName") + "\n" +
                                "Author Name: " + rs.getString("authorName") + "\n" +
                                "Student Name: " + rs.getString("studentName") + "\n" +
                                "Student Address: " + rs.getString("studentAddr") + "\n" +
                                "Book Issue Date: " + rs.getDate("bookIssueDate");
                        JOptionPane.showMessageDialog(this, bookDetails, "Book Details", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(this, "No book found with the given ID.", "Not Found", JOptionPane.INFORMATION_MESSAGE);
                    }
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Book ID must be a number.", "Input Error", JOptionPane.ERROR_MESSAGE);
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void viewBooks() {
        if (connection == null) {
            JOptionPane.showMessageDialog(this, "Not connected to the database.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        StringBuilder allBooks = new StringBuilder();
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM books")) {
            while (rs.next()) {
                allBooks.append("Book ID: ").append(rs.getInt("bookId")).append("\n")
                        .append("Book Name: ").append(rs.getString("bookName")).append("\n")
                        .append("Author Name: ").append(rs.getString("authorName")).append("\n")
                        .append("Student Name: ").append(rs.getString("studentName")).append("\n")
                        .append("Student Address: ").append(rs.getString("studentAddr")).append("\n")
                        .append("Book Issue Date: ").append(rs.getDate("bookIssueDate")).append("\n\n");
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }

        JTextArea textArea = new JTextArea(allBooks.toString());
        textArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(textArea);
        JDialog viewBooksDialog = new JDialog(this, "View Books", true);
        viewBooksDialog.add(scrollPane);
        viewBooksDialog.setSize(500, 400);
        viewBooksDialog.setLocationRelativeTo(this);
        viewBooksDialog.setVisible(true);
    }

    private void deleteBooks() {
        if (connection == null) {
            JOptionPane.showMessageDialog(this, "Not connected to the database.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String bookIdStr = JOptionPane.showInputDialog(this, "Enter Book ID to delete:");
        if (bookIdStr != null && !bookIdStr.trim().isEmpty()) {
            try {
                int bookId = Integer.parseInt(bookIdStr.trim());
                String sql = "DELETE FROM books WHERE bookId = ?";
                try (PreparedStatement ps = connection.prepareStatement(sql)) {
                    ps.setInt(1, bookId);
                    int rowsAffected = ps.executeUpdate();
                    if (rowsAffected > 0) {
                        JOptionPane.showMessageDialog(this, "Book deleted successfully!");
                    } else {
                        JOptionPane.showMessageDialog(this, "No book found with the given ID.", "Not Found", JOptionPane.INFORMATION_MESSAGE);
                    }
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Book ID must be a number.", "Input Error", JOptionPane.ERROR_MESSAGE);
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
