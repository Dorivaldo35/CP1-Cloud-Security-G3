package br.edu.grupo3;

import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Random;

public class Vulneravel {

    private static final String SENHA_BANCO = "admin123";

    public ResultSet buscarUsuario(String nome) throws Exception {
        Connection c = DriverManager.getConnection("jdbc:h2:mem:db", "sa", SENHA_BANCO);
        Statement st = c.createStatement();
        return st.executeQuery("SELECT * FROM usuarios WHERE nome = '" + nome + "'");
    }

    public void ping(String host) throws Exception {
        Runtime.getRuntime().exec("ping -c 1 " + host);
    }

    public byte[] hashSenha(String senha) throws Exception {
        return MessageDigest.getInstance("MD5").digest(senha.getBytes());
    }

    public int gerarToken() {
        return new Random().nextInt();
    }
}