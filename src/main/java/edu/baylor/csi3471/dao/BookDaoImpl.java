package edu.baylor.csi3471.dao;

import edu.baylor.csi3471.model.Book;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class BookDaoImpl implements BookDao {

    private static final Logger LOGGER =
            Logger.getLogger(BookDaoImpl.class.getName());

    private static final String URL = "jdbc:sqlite:campus_books.db";

    public BookDaoImpl() {
        String sql = "CREATE TABLE IF NOT EXISTS books (" +
                "id INTEGER PRIMARY KEY, title TEXT NOT NULL, author TEXT NOT NULL)";

        try (Connection con = DriverManager.getConnection(URL);
             Statement stmt = con.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            LOGGER.severe(e.getMessage());
            throw new DataAccessException("Database error", e);
        }
    }

    @Override
    public Book findById(int id) {
        String sql = "SELECT * FROM books WHERE id = ?";

        try (Connection con = DriverManager.getConnection(URL);
             PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Book(rs.getInt("id"),
                            rs.getString("title"), rs.getString("author"));
                }
                return null;
            }
        } catch (SQLException e) {
            LOGGER.severe(e.getMessage());
            throw new DataAccessException("Database error", e);
        }
    }

    @Override
    public List<Book> findByAuthor(String author) {
        List<Book> books = new ArrayList<>();
        String sql = "SELECT * FROM books WHERE author = ?";

        try (Connection con = DriverManager.getConnection(URL);
             PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, author);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    books.add(new Book(rs.getInt("id"),
                            rs.getString("title"), rs.getString("author")));
                }
            }
            return books;
        } catch (SQLException e) {
            LOGGER.severe(e.getMessage());
            throw new DataAccessException("Database error", e);
        }
    }

    @Override
    public void insert(Book book) {
        String sql = "INSERT INTO books VALUES (?, ?, ?)";

        try (Connection con = DriverManager.getConnection(URL);
             PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setInt(1, book.getId());
            stmt.setString(2, book.getTitle());
            stmt.setString(3, book.getAuthor());
            stmt.executeUpdate();
        } catch (SQLException e) {
            LOGGER.severe(e.getMessage());
            throw new DataAccessException("Database error", e);
        }
    }

    @Override
    public void update(Book book) {
        String sql = "UPDATE books SET title = ?, author = ? WHERE id = ?";

        try (Connection con = DriverManager.getConnection(URL);
             PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, book.getTitle());
            stmt.setString(2, book.getAuthor());
            stmt.setInt(3, book.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            LOGGER.severe(e.getMessage());
            throw new DataAccessException("Database error", e);
        }
    }

    @Override
    public void delete(int id) {
        String sql = "DELETE FROM books WHERE id = ?";

        try (Connection con = DriverManager.getConnection(URL);
             PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            LOGGER.severe(e.getMessage());
            throw new DataAccessException("Database error", e);
        }
    }
}