package gui;

import dao.MovieDAO;
import models.Movie;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.nio.file.Files;
import java.sql.Date;
import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.List;

public class MovieManagementPanel extends JPanel {
    private final Color primaryColor = new Color(41, 128, 185);
    private final Color backgroundColor = new Color(236, 240, 241);
    private final Color whiteColor = Color.WHITE;
    private final Color textColor = new Color(44, 62, 80);

    private JTable movieTable;
    private DefaultTableModel tableModel;
    private JTextField titleField, genreField, directorField, durationField, ratingField, languageField;
    private JTextArea sinopsisArea;
    private JSpinner releaseDateSpinner;
    private byte[] posterImageBytes;

    private MovieDAO movieDAO = new MovieDAO();

    public MovieManagementPanel() {
        setLayout(new BorderLayout());
        setBackground(backgroundColor);
        initializeComponents();
        loadMovieData();
    }

    private void initializeComponents() {
        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(whiteColor);
        headerPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel titleLabel = new JLabel("Movie Management");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 22));
        titleLabel.setForeground(textColor);
        headerPanel.add(titleLabel, BorderLayout.WEST);

        JButton refreshBtn = createActionButton("Refresh", primaryColor);
        refreshBtn.addActionListener(e -> loadMovieData());
        headerPanel.add(refreshBtn, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);

        // Split Panel
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setResizeWeight(0.65);
        splitPane.setBorder(null);

        splitPane.setLeftComponent(createTablePanel());
        splitPane.setRightComponent(new JScrollPane(createFormPanel()));

        add(splitPane, BorderLayout.CENTER);
    }

    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(0, 20, 20, 10));

        String[] cols = {"ID", "Title", "Genre", "Duration", "Director", "Rating", "Release Date", "Language"};
        tableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };

        movieTable = new JTable(tableModel);
        movieTable.setRowHeight(30);
        movieTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) loadSelectedMovie();
        });

        panel.add(new JScrollPane(movieTable), BorderLayout.CENTER);
        return panel;
    }

    private JPanel createFormPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(whiteColor);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        panel.add(new JLabel("Title:"));
        panel.add(titleField = new JTextField());

        panel.add(new JLabel("Sinopsis:"));
        panel.add(new JScrollPane(sinopsisArea = new JTextArea(3, 20)));

        panel.add(new JLabel("Genre:"));
        panel.add(genreField = new JTextField());

        panel.add(new JLabel("Duration (HH:mm:ss):"));
        panel.add(durationField = new JTextField());

        panel.add(new JLabel("Director:"));
        panel.add(directorField = new JTextField());

        panel.add(new JLabel("Age Rating:"));
        panel.add(ratingField = new JTextField());

        panel.add(new JLabel("Language:"));
        panel.add(languageField = new JTextField());

        panel.add(new JLabel("Release Date:"));
        releaseDateSpinner = new JSpinner(new SpinnerDateModel());
        releaseDateSpinner.setEditor(new JSpinner.DateEditor(releaseDateSpinner, "yyyy-MM-dd"));
        panel.add(releaseDateSpinner);

        JButton uploadPosterBtn = new JButton("Upload Poster");
        uploadPosterBtn.addActionListener(e -> choosePosterImage());
        panel.add(uploadPosterBtn);

        JPanel btnPanel = new JPanel(new FlowLayout());
        JButton addBtn = createActionButton("Add", new Color(46, 204, 113));
        addBtn.addActionListener(e -> addMovie());
        btnPanel.add(addBtn);

        JButton updateBtn = createActionButton("Update", new Color(241, 196, 15));
        updateBtn.addActionListener(e -> updateMovie());
        btnPanel.add(updateBtn);

        JButton deleteBtn = createActionButton("Delete", new Color(231, 76, 60));
        deleteBtn.addActionListener(e -> deleteMovie());
        btnPanel.add(deleteBtn);

        JButton clearBtn = createActionButton("Clear", new Color(149, 165, 166));
        clearBtn.addActionListener(e -> clearForm());
        btnPanel.add(clearBtn);

        panel.add(btnPanel);
        return panel;
    }

    private JButton createActionButton(String text, Color color) {
        JButton btn = new JButton(text);
        btn.setBackground(color);
        btn.setForeground(Color.WHITE);
        return btn;
    }

    private void choosePosterImage() {
        JFileChooser chooser = new JFileChooser();
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                File file = chooser.getSelectedFile();
                posterImageBytes = Files.readAllBytes(file.toPath());
                JOptionPane.showMessageDialog(this, "Poster berhasil diupload!");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Gagal upload poster: " + ex.getMessage());
            }
        }
    }

    private void loadMovieData() {
        tableModel.setRowCount(0);
        List<Movie> movies = movieDAO.getAllMovies();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        for (Movie m : movies) {
            tableModel.addRow(new Object[]{
                    m.getId(),
                    m.getTitle(),
                    m.getGenre(),
                    m.getDuration(),
                    m.getDirector(),
                    m.getRatingUsia(),
                    m.getReleasedDate() != null ? sdf.format(m.getReleasedDate()) : "",
                    m.getLanguage()
            });
        }
    }

    private void loadSelectedMovie() {
        int row = movieTable.getSelectedRow();
        if (row >= 0) {
            int id = (int) tableModel.getValueAt(row, 0);
            Movie movie = movieDAO.getMovieById(id);
            if (movie != null) {
                titleField.setText(movie.getTitle());
                sinopsisArea.setText(movie.getSinopsis());
                genreField.setText(movie.getGenre());
                durationField.setText(movie.getDuration().toString());
                directorField.setText(movie.getDirector());
                ratingField.setText(movie.getRatingUsia());
                languageField.setText(movie.getLanguage());
                releaseDateSpinner.setValue(movie.getReleasedDate());
                posterImageBytes = movie.getPosterImage();
            }
        }
    }

    private void addMovie() {
        if (!validateForm()) return;
        Movie movie = getMovieFromForm();
        int newId = movieDAO.addMovie(movie);
        if (newId > 0) {
            JOptionPane.showMessageDialog(this, "Movie berhasil ditambahkan!");
            loadMovieData();
            clearForm();
        }
    }

    private void updateMovie() {
        int row = movieTable.getSelectedRow();
        if (row >= 0 && validateForm()) {
            int id = (int) tableModel.getValueAt(row, 0);
            Movie movie = getMovieFromForm();
            movie.setId(id);
            if (movieDAO.updateMovie(movie)) {
                JOptionPane.showMessageDialog(this, "Movie berhasil diupdate!");
                loadMovieData();
                clearForm();
            }
        }
    }

    private void deleteMovie() {
        int row = movieTable.getSelectedRow();
        if (row >= 0) {
            int id = (int) tableModel.getValueAt(row, 0);
            if (movieDAO.deleteMovie(id)) {
                JOptionPane.showMessageDialog(this, "Movie berhasil dihapus!");
                loadMovieData();
                clearForm();
            }
        }
    }

    private Movie getMovieFromForm() {
        Movie movie = new Movie();
        movie.setTitle(titleField.getText().trim());
        movie.setSinopsis(sinopsisArea.getText().trim());
        movie.setGenre(genreField.getText().trim());
        movie.setDuration(Time.valueOf(durationField.getText().trim())); // format HH:mm:ss
        movie.setDirector(directorField.getText().trim());
        java.util.Date utilDate = (java.util.Date) releaseDateSpinner.getValue();
        movie.setReleasedDate(new Date(utilDate.getTime()));
        movie.setRatingUsia(ratingField.getText().trim());
        movie.setLanguage(languageField.getText().trim());
        movie.setPosterImage(posterImageBytes);
        return movie;
    }

    private void clearForm() {
        titleField.setText("");
        sinopsisArea.setText("");
        genreField.setText("");
        durationField.setText("");
        directorField.setText("");
        ratingField.setText("");
        languageField.setText("");
        releaseDateSpinner.setValue(new java.util.Date());
        posterImageBytes = null;
        movieTable.clearSelection();
    }

    private boolean validateForm() {
        if (titleField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Title wajib diisi!");
            return false;
        }
        if (durationField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Duration wajib diisi (HH:mm:ss)!");
            return false;
        }
        try {
            Time.valueOf(durationField.getText().trim());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Format duration salah! Gunakan HH:mm:ss");
            return false;
        }
        return true;
    }
}
