package edu.baylor.csi3471.dao;

import edu.baylor.csi3471.model.Book;
import java.util.List;

public interface BookDao {
    Book findById(int id);

    List<Book> findByAuthor(String author);

    void insert(Book book);

    void update(Book book);

    void delete(int id);
}