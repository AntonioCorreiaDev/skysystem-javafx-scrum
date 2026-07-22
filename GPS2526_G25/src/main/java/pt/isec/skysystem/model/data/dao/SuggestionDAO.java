package pt.isec.skysystem.model.data.dao;

import pt.isec.skysystem.model.data.DataBaseManager;
import pt.isec.skysystem.model.data.Suggestion;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object para a entidade Suggestion.
 * Responsável por todas as operações de persistência (CRUD) na tabela Suggestions.
 */
public class SuggestionDAO {

    private static final String TABLE_NAME = "SUGGESTIONS";

    public boolean save(String destination, String imgUrl, String tags, String reason, String priceRange, long userId) {
        String sql = "INSERT INTO " + TABLE_NAME +
                " (user_id, destination_iata, tags, reason, price_range, image_url) VALUES (?, ?, ?, ?,?,?)";

        try (Connection conn = DataBaseManager.getConnection(); // Utiliza o seu gestor de conexão
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // Mapeamento dos campos do objeto Suggestion para os placeholders do SQL
            pstmt.setLong(1, userId);
            pstmt.setString(2, destination);
            pstmt.setString(3, tags != null ? tags : " ");
            pstmt.setString(4, reason != null ? reason : " ");
            pstmt.setString(5, priceRange != null ? priceRange : " ");
            pstmt.setString(6, imgUrl);

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;

        } catch (Exception e) {
            System.err.println("Erro ao inserir Suggestion na BD SQLite: " + e.getMessage());
            return false;
        }
    }

    /**
     * Obtém todas as sugestões da base de dados.
     * @return Uma lista de objetos Suggestion.
     */
    public List<Suggestion> getFavoritesSuggestions(long userId) {
        List<Suggestion> suggestions = new ArrayList<>();

        // A tabela Suggestions deve ter a coluna 'user_id' para esta query funcionar.
        String sql = "SELECT destination_iata, tags, reason, price_range, image_url FROM " + TABLE_NAME + " WHERE user_id = ?";

        // Declaramos apenas os recursos no try-with-resources (Connection e PreparedStatement)
        try (Connection conn = DataBaseManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // EXECUTÁVEIS AQUI:
            pstmt.setLong(1, userId);

            // Declaramos o ResultSet dentro do bloco para manter o escopo correto
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    // Criação do objeto Suggestion a partir dos resultados da base de dados
                    String destination = rs.getString("destination_iata");
                    String tags = rs.getString("tags");
                    String reason = rs.getString("reason");
                    String priceRange = rs.getString("price_range");
                    String imageUrl = rs.getString("image_url");

                    Suggestion suggestion = new Suggestion(destination, tags, reason, priceRange, imageUrl);
                    suggestions.add(suggestion);
                }
            }

        } catch (Exception e) {
            System.err.println("Erro ao carregar Suggestions da BD SQLite para o utilizador " + userId + ": " + e.getMessage());
        }
        return suggestions;
    }

    public boolean removeFavoriteSuggestion(String destinationName, long userId) {
        String sql = "DELETE FROM " + TABLE_NAME + " WHERE destination_iata = ? AND user_id = ?";

        try (Connection conn = DataBaseManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, destinationName);
            pstmt.setLong(2, userId);

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;

        } catch (Exception e) {
            System.err.println("Erro ao remover Suggestion da BD SQLite: " + e.getMessage());
            return false;
        }
    }

}