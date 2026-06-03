import java.sql.*;
import java.util.ArrayList;

import javafx.collections.ObservableList;

public class DatabaseHelper {
    private static final String URL  = "jdbc:mysql://localhost:3306/klinik_db";
    private static final String USER = "root";
    private static final String PASS = "Salsabila123!"; // kosong untuk Laragon

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASS);
    }

    public static void simpanPasien(Pasien p) {
        String sql = "INSERT IGNORE INTO pasien VALUES (?,?,?,?,?,?)";
        try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, p.getIdPasien());
            ps.setString(2, p.getNama());
            ps.setString(3, p.getAlamat());
            ps.setString(4, p.getNoTelp());
            ps.setString(5, p.getTanggalLahir());
            ps.setString(6, p.getJenisKelamin());
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public static void simpanDokter(Dokter d) {
        String sql = "INSERT IGNORE INTO dokter VALUES (?,?,?,?)";
        try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, d.getIdDokter());
            ps.setString(2, d.getNama());
            ps.setString(3, d.getSpesialisasi());
            ps.setString(4, d.getNoTelp());
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public static void simpanUser(User u) {
        String sql = "INSERT IGNORE INTO users VALUES (?,?,?)";
        try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, u.getUsername());
            ps.setString(2, u.getPassword());  // tambahkan getPassword() di class User
            ps.setString(3, u.getRole());
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public static void simpanObat(Obat o) {
        String sql = "INSERT IGNORE INTO obat VALUES (?,?,?,?,?)";
        try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, o.getIdObat());
            ps.setString(2, o.getNamaObat());
            ps.setString(3, o.getJenis());
            ps.setInt(4, o.getStok());
            ps.setDouble(5, o.getHarga());
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public static void updateObatStok(Obat o) {
        String sql = "UPDATE obat SET stok = ? WHERE id_obat = ?";
        try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, o.getStok());
            ps.setString(2, o.getIdObat());
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public static void loadPasien(ObservableList<Pasien> list) {
    String sql = "SELECT * FROM pasien";
    try (Connection c = getConnection();
         Statement st = c.createStatement();
         ResultSet rs = st.executeQuery(sql)) {
        list.clear();
        while (rs.next())
            list.add(new Pasien(
                rs.getString("id_pasien"),
                rs.getString("nama"),
                rs.getString("alamat"),
                rs.getString("no_telp"),
                rs.getString("tanggal_lahir"),
                rs.getString("jenis_kelamin")
            ));
    } catch (SQLException e) { e.printStackTrace(); }
}

public static void loadDokter(ObservableList<Dokter> list) {
    String sql = "SELECT * FROM dokter";
    try (Connection c = getConnection();
         Statement st = c.createStatement();
         ResultSet rs = st.executeQuery(sql)) {
        list.clear();
        while (rs.next())
            list.add(new Dokter(
                rs.getString("id_dokter"),
                rs.getString("nama"),
                rs.getString("spesialisasi"),
                rs.getString("no_telp")
            ));
    } catch (SQLException e) { e.printStackTrace(); }
}
public static void loadObat(ObservableList<Obat> list) {
    String sql = "SELECT * FROM obat";
    try (Connection c = getConnection();
         Statement st = c.createStatement();
         ResultSet rs = st.executeQuery(sql)) {
        list.clear();
        while (rs.next())
            list.add(new Obat(
                rs.getString("id_obat"),
                rs.getString("nama"),
                rs.getString("kategori"),
                rs.getInt("stok"),
                rs.getInt("harga")
            ));
    } catch (SQLException e) { e.printStackTrace(); }
}

public static void loadUsers(ArrayList<User> list) {
    String sql = "SELECT * FROM users";
    try (Connection c = getConnection();
         Statement st = c.createStatement();
         ResultSet rs = st.executeQuery(sql)) {
        list.clear();
        while (rs.next())
            list.add(new User(
                rs.getString("username"),
                rs.getString("password"),
                rs.getString("role")
            ));
    } catch (SQLException e) { e.printStackTrace(); }
}
public static void loadJadwal(ObservableList<JadwalDokter> list) {
    String sql = "SELECT j.*, d.nama, d.spesialisasi, d.no_telp FROM jadwal_dokter j JOIN dokter d ON j.id_dokter = d.id_dokter";
    try (Connection c = getConnection();
         Statement st = c.createStatement();
         ResultSet rs = st.executeQuery(sql)) {
        list.clear();
        while (rs.next()) {
            Dokter dk = new Dokter(rs.getString("id_dokter"), rs.getString("nama"),
                rs.getString("spesialisasi"), rs.getString("no_telp"));
            list.add(new JadwalDokter(rs.getString("id_jadwal"), dk,
                rs.getString("hari"), rs.getString("jam_mulai"),
                rs.getString("jam_selesai"), rs.getInt("kuota")));
        }
    } catch (SQLException e) { e.printStackTrace(); }
}
public static void loadRekamMedis(ObservableList<RekamMedis> list) {
    String sql = "SELECT * FROM rekam_medis";
    try (Connection c = getConnection();
         Statement st = c.createStatement();
         ResultSet rs = st.executeQuery(sql)) {
        list.clear();
        while (rs.next())
            list.add(new RekamMedis(
                rs.getString("id_rm"),
                null, // pasien di-load terpisah jika perlu
                null, // dokter di-load terpisah jika perlu
                rs.getString("tanggal"),
                rs.getString("diagnosa"),
                rs.getString("catatan")
            ));
    } catch (SQLException e) { e.printStackTrace(); }
}
public static void loadPembayaran(ObservableList<Pembayaran> list) {
    String sql = "SELECT * FROM pembayaran";
    try (Connection c = getConnection();
         Statement st = c.createStatement();
         ResultSet rs = st.executeQuery(sql)) {
        list.clear();
        while (rs.next()) {
            // Buat RekamMedis dummy karena Pembayaran butuh objek RekamMedis
            RekamMedis rmDummy = new RekamMedis(
                rs.getString("id_pasien"), // pakai id_pasien sebagai id sementara
                null, null,
                rs.getString("tanggal"),
                "-", "-"
            );
            Pembayaran p = new Pembayaran(
                rs.getString("id_bayar"),
                rmDummy,
                rs.getInt("total"),
                rs.getString("tanggal")
            );
            p.setStatus(rs.getString("status"));
            list.add(p);
        }
    } catch (SQLException e) { e.printStackTrace(); }
}
public static int getJumlahAntreanDariDB(String idDokter) {
    String sql = "SELECT COUNT(*) FROM rekam_medis WHERE id_dokter = ?";
    try (Connection c = getConnection();
         PreparedStatement ps = c.prepareStatement(sql)) {
        ps.setString(1, idDokter);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) return rs.getInt(1);
    } catch (SQLException e) { e.printStackTrace(); }
    return 0;
}
}