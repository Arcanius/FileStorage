package com.example.filestorage;

import java.io.*;
import java.sql.*;
import java.util.Scanner;

public class App {
    private static Connection connection;
    private static Scanner scanner;

    public static void main(String[] args) {
        try {
            scanner = new Scanner(System.in);
            Class.forName("com.mysql.cj.jdbc.Driver").getDeclaredConstructor().newInstance();
            connection = getConnection();
            connection.setAutoCommit(false);
            while (true) {
                System.out.print("Enter list, download, upload or exit: ");
                String action = scanner.nextLine();
                if (action.equals("exit")) {
                    break;
                }
                switch (action) {
                    case "upload" -> uploadFile();
                    case "download" -> downloadFile();
                    case "list" -> listFiles();
                    default -> System.out.println("You entered wrong command");
                }
            }
            connection.close();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    private static Connection getConnection() throws SQLException {
        String url = "jdbc:mysql://localhost/FileStorage";
        String username = "test";
        String password = "password";
        return DriverManager.getConnection(url, username, password);
    }

    private static void uploadFile() {
        try {
            System.out.print("Enter path to file: ");
            String path = scanner.nextLine();
            File file = new File(path);
            FileInputStream fileInputStream = new FileInputStream(file);
            PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO " +
                    "Files (FileName, FileContent) VALUES (?, ?)");
            preparedStatement.setString(1, file.getName());
            preparedStatement.setBinaryStream(2, fileInputStream);
            preparedStatement.executeUpdate();
            connection.commit();
            System.out.println("File " + file.getName() + " is saved in database");
        } catch (FileNotFoundException exception) {
            System.out.println("File not found");
        } catch (SQLException exception) {
            System.out.println("Database connection error");
        }
    }

    private static void downloadFile() {
        try {
            System.out.print("Enter name of file: ");
            String fileName = scanner.nextLine();
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM Files WHERE FileName = '" + fileName + "'");
            if (resultSet.next()) {
                String name = resultSet.getString("FileName");
                Blob file = resultSet.getBlob("FileContent");
                InputStream inputStream = file.getBinaryStream();
                byte[] bytes = new byte[inputStream.available()];
                inputStream.read(bytes);
                System.out.print("Enter path to folder where file to be saved: ");
                String folder = scanner.nextLine();
                OutputStream outputStream = new FileOutputStream(folder + "\\" + name);
                outputStream.write(bytes);
                outputStream.close();
                System.out.println("File " + name + " is saved in " + folder);
            } else {
                System.out.println("File not found");
            }
        } catch (IOException exception) {
            System.out.println("Wrong folder name");
        } catch (SQLException exception) {
            System.out.println("Database connection error");
        }
    }

    private static void listFiles() {
        try {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM Files");
            System.out.println("List of stored files: ");
            while (resultSet.next()) {
                int id = resultSet.getInt("FileID");
                String name = resultSet.getString("FileName");
                System.out.println(id + "\t" + name);
            }
        } catch (SQLException exception) {
            System.out.println("Database connection error");
        }
    }
}
