import java.sql.*;
import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

public class KlinikGUI extends Application {

    private ArrayList<User> users = new ArrayList<>();
    private ObservableList<Pasien> daftarPasien = FXCollections.observableArrayList();
    private ObservableList<Dokter> daftarDokter = FXCollections.observableArrayList();
    private ObservableList<Obat> daftarObat = FXCollections.observableArrayList();
    private ArrayList<JadwalDokter> daftarJadwal = new ArrayList<>();
    private ArrayList<RekamMedis> daftarRekamMedis = new ArrayList<>();
    private ArrayList<Resep> daftarResep = new ArrayList<>();
    private ArrayList<Pembayaran> daftarPembayaran = new ArrayList<>();
    private Antrean antrean = new Antrean();

    private Runnable refreshAntreanTabel;
    private Runnable refreshDashboard;

    private User userLogin;
    private Pasien pasienLogin;
    private Dokter dokterLogin;
    private Stage primaryStage;

    private int idPasienCounter = 1;
    private int idDokterCounter = 3;
    private int idObatCounter = 4;
    private int idRMCounter = 1;
    private int idBayarCounter = 1;
    private int idJadwalCounter = 3;
    private int idResepCounter = 1;

    @Override
    public void start(Stage stage) {initDataAwal();
        this.primaryStage = stage;
        initDataAwal();
        showLogin();
    }

// ===================== DATA AWAL (UPDATED WITH DEFAULT QUEUE) =====================
    private void initDataAwal() {
    try (Connection conn = DatabaseHelper.getConnection()) {

        // Load users
        ResultSet rsU = conn.createStatement().executeQuery("SELECT * FROM users");
        while (rsU.next())
            users.add(new User(rsU.getString("username"), rsU.getString("password"), rsU.getString("role")));

        // Load dokter
        ResultSet rsD = conn.createStatement().executeQuery("SELECT * FROM dokter");
        while (rsD.next())
            daftarDokter.add(new Dokter(rsD.getString("id_dokter"), rsD.getString("nama"),
                rsD.getString("spesialisasi"), rsD.getString("no_telp")));

        // ← PINDAH KE SINI, setelah semua dokter selesai di-load
        for (Dokter d : daftarDokter) {
            int sudahAda = DatabaseHelper.getJumlahAntreanDariDB(d.getIdDokter());
            antrean.inisialisasiCounter(d.getIdDokter(), sudahAda);
        }

        // Load pasien
        ResultSet rsP = conn.createStatement().executeQuery("SELECT * FROM pasien");
        while (rsP.next())
            daftarPasien.add(new Pasien(rsP.getString("id_pasien"), rsP.getString("nama"),
                rsP.getString("alamat"), rsP.getString("no_telp"),
                rsP.getString("tanggal_lahir"), rsP.getString("jenis_kelamin")));

        // Load obat
        ResultSet rsO = conn.createStatement().executeQuery("SELECT * FROM obat");
        while (rsO.next())
            daftarObat.add(new Obat(rsO.getString("id_obat"), rsO.getString("nama"),
                rsO.getString("kategori"), rsO.getInt("stok"), rsO.getInt("harga")));

        // Load jadwal
        ResultSet rsJ = conn.createStatement().executeQuery(
            "SELECT j.*, d.nama, d.spesialisasi, d.no_telp FROM jadwal_dokter j JOIN dokter d ON j.id_dokter = d.id_dokter");
        while (rsJ.next()) {
            Dokter dk = new Dokter(rsJ.getString("id_dokter"), rsJ.getString("nama"),
                rsJ.getString("spesialisasi"), rsJ.getString("no_telp"));
            daftarJadwal.add(new JadwalDokter(rsJ.getString("id_jadwal"), dk,
                rsJ.getString("hari"), rsJ.getString("jam_mulai"),
                rsJ.getString("jam_selesai"), rsJ.getInt("kuota")));
        }

        // Hitung counter ID otomatis
        idPasienCounter = daftarPasien.size() + 1;
        idDokterCounter = daftarDokter.size() + 1;
        idObatCounter   = daftarObat.size() + 1;
        idJadwalCounter = daftarJadwal.size() + 1;

    } catch (SQLException e) {
        showAlert("Error DB", "Gagal koneksi database:\n" + e.getMessage());
        users.add(new User("admin", "admin123", "admin"));
    }
}

    // ===================== LOGIN =====================
    private void showLogin() {
        primaryStage.setTitle("Aplikasi Klinik - Login");

        VBox root = new VBox(15);
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: #2c3e50;");

        Label lblJudul = new Label("APLIKASI KLINIK");
        lblJudul.setFont(Font.font("Arial", FontWeight.BOLD, 28));
        lblJudul.setTextFill(Color.WHITE);

        Label lblSub = new Label("Sistem Manajemen Klinik");
        lblSub.setFont(Font.font("Arial", 14));
        lblSub.setTextFill(Color.web("#95a5a6"));

        VBox formBox = new VBox(12);
        formBox.setAlignment(Pos.CENTER);
        formBox.setPadding(new Insets(30));
        formBox.setMaxWidth(350);
        formBox.setStyle("-fx-background-color: #34495e; -fx-background-radius: 10;");

        Label lblForm = new Label("Silakan Login");
        lblForm.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        lblForm.setTextFill(Color.WHITE);

        TextField txtUser = new TextField();
        txtUser.setPromptText("Username");
        txtUser.setStyle("-fx-background-radius: 5; -fx-padding: 10; -fx-font-size: 14;");

        PasswordField txtPass = new PasswordField();
        txtPass.setPromptText("Password");
        txtPass.setStyle("-fx-background-radius: 5; -fx-padding: 10; -fx-font-size: 14;");

        Button btnLogin = new Button("LOGIN");
        btnLogin.setMaxWidth(Double.MAX_VALUE);
        btnLogin.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; " +
                "-fx-font-size: 14; -fx-font-weight: bold; -fx-background-radius: 5; -fx-padding: 10;");
        btnLogin.setCursor(javafx.scene.Cursor.HAND);

        Label lblInfo = new Label("admin/admin123 | dokter1/dok123 | pasien: P001/P001");
        lblInfo.setFont(Font.font("Arial", 11));
        lblInfo.setTextFill(Color.web("#7f8c8d"));

        Label lblError = new Label("");
        lblError.setTextFill(Color.RED);

        btnLogin.setOnAction(e -> {
            String u = txtUser.getText().trim();
            String p = txtPass.getText().trim();
            for (User usr : users) {
                if (usr.login(u, p)) {
                    userLogin = usr;
                    pasienLogin = null;
                    dokterLogin = null;

                    if (usr.getRole().equals("pasien")) {
                        for (Pasien ps : daftarPasien) {
                            if (ps.getIdPasien().equals(u)) {
                                pasienLogin = ps;
                                break;
                            }
                        }
                    } else if (usr.getRole().equals("dokter")) {
                        // Mencari dokter berdasarkan ID secara langsung (untuk dokter baru)
                        for (Dokter dk : daftarDokter) {
                            if (dk.getIdDokter().equalsIgnoreCase(u)) {
                                dokterLogin = dk;
                                break;
                            }
                        }
                        // Fallback ke logika index bawaan jika menggunakan data awal "dokter1"
                        if (dokterLogin == null) {
                            int idx = 0;
                            try { idx = Integer.parseInt(u.replace("dokter", "")) - 1; } catch (Exception ex) {}
                            if (idx >= 0 && idx < daftarDokter.size()) dokterLogin = daftarDokter.get(idx);
                        }
                    }

                    showMain();
                    return;
                }
            }
            lblError.setText("Username atau password salah!");
        });

        txtPass.setOnAction(e -> btnLogin.fire());

        formBox.getChildren().addAll(lblForm, txtUser, txtPass, btnLogin, lblError);
        root.getChildren().addAll(lblJudul, lblSub, formBox, lblInfo);

        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.setMaximized(true);
        primaryStage.show();
    }

    // ===================== MAIN =====================
    private void showMain() {
    String role = userLogin.getRole();
    primaryStage.setTitle("Aplikasi Klinik - " + userLogin.getUsername() + " (" + role + ")");

    BorderPane root = new BorderPane();

    VBox sidebar = new VBox(5);
    sidebar.setPrefWidth(200);
    sidebar.setStyle("-fx-background-color: #2c3e50;");
    sidebar.setPadding(new Insets(0, 0, 10, 0));

    Label lblApp = new Label("  KLINIK APP");
    lblApp.setFont(Font.font("Arial", FontWeight.BOLD, 16));
    lblApp.setTextFill(Color.WHITE);
    lblApp.setPadding(new Insets(20, 10, 20, 10));
    lblApp.setMaxWidth(Double.MAX_VALUE);
    lblApp.setStyle("-fx-background-color: #1a252f;");

    Label lblUser = new Label("  " + userLogin.getUsername() + " [" + role + "]");
    lblUser.setFont(Font.font("Arial", 12));
    lblUser.setTextFill(Color.web("#95a5a6"));
    lblUser.setPadding(new Insets(5, 10, 15, 10));

    StackPane konten = new StackPane();
    konten.setStyle("-fx-background-color: #ecf0f1;");

    // ← BUILD SEMUA PANEL SEKALI di sini
    VBox panelDashboard    = buatDashboard();
    VBox panelPasien       = buatPanelPasien();
    VBox panelDokter       = buatPanelDokter();
    VBox panelAntrean      = buatPanelAntrean(); // ← refreshAntreanTabel di-assign di sini
    VBox panelObat         = buatPanelObat();
    VBox panelJadwal       = buatPanelJadwal();
    VBox panelPembayaran   = buatPanelPembayaran();
    VBox panelRekamMedis   = buatPanelRekamMedis(false);
    VBox panelResep        = buatPanelResep(false);
    VBox panelNomorAntrean = buatPanelNomorAntrean();
    VBox panelRMPribadi    = buatPanelRekamMedisPribadi();
    VBox panelResepPasien  = buatPanelResepPasien();

    String[] menus;
    if (role.equals("admin")) {
        menus = new String[]{"Dashboard", "Pasien", "Dokter", "Antrean", "Obat", "Jadwal", "Pembayaran"};
    } else if (role.equals("dokter")) {
        menus = new String[]{"Dashboard", "Rekam Medis", "Resep Obat"};
    } else {
        menus = new String[]{"Nomor Antrean", "Rekam Medis Pribadi", "Resep Saya"};
    }

    sidebar.getChildren().addAll(lblApp, lblUser);

    for (String menu : menus) {
        Button btn = buatTombolSidebar(menu);
        btn.setOnAction(e -> {
            konten.getChildren().clear();
            switch (menu) {
                case "Dashboard":           konten.getChildren().add(panelDashboard); break;
                case "Pasien":              konten.getChildren().add(panelPasien); break;
                case "Dokter":              konten.getChildren().add(panelDokter); break;
                case "Antrean":             konten.getChildren().add(panelAntrean); break;
                case "Obat":                konten.getChildren().add(panelObat); break;
                case "Jadwal":              konten.getChildren().add(panelJadwal); break;
                case "Pembayaran":          konten.getChildren().add(panelPembayaran); break;
                case "Rekam Medis":         konten.getChildren().add(panelRekamMedis); break;
                case "Resep Obat":          konten.getChildren().add(panelResep); break;
                case "Nomor Antrean":       konten.getChildren().add(panelNomorAntrean); break;
                case "Rekam Medis Pribadi": konten.getChildren().add(panelRMPribadi); break;
                case "Resep Saya":          konten.getChildren().add(panelResepPasien); break;
            }
        });
        sidebar.getChildren().add(btn);
    }

    Region spacer = new Region();
    VBox.setVgrow(spacer, Priority.ALWAYS);
    sidebar.getChildren().add(spacer);

    Button btnLogout = buatTombolSidebar("Logout");
    btnLogout.setStyle("-fx-background-color: #c0392b; -fx-text-fill: white; " +
            "-fx-font-size: 13; -fx-alignment: CENTER-LEFT; -fx-padding: 12 15; " +
            "-fx-border-width: 0; -fx-background-radius: 0;");
    btnLogout.setOnAction(e -> showLogin());
    sidebar.getChildren().add(btnLogout);

    // Panel default saat pertama login
    if (role.equals("admin")) konten.getChildren().add(panelDashboard);
    else if (role.equals("dokter")) konten.getChildren().add(panelRekamMedis);
    else konten.getChildren().add(panelNomorAntrean);

    root.setLeft(sidebar);
    root.setCenter(konten);

    Scene scene = new Scene(root);
    primaryStage.setScene(scene);
    primaryStage.setMaximized(true);
}

    private Button buatTombolSidebar(String teks) {
        Button btn = new Button(teks);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setStyle("-fx-background-color: #2c3e50; -fx-text-fill: white; " +
                "-fx-font-size: 13; -fx-alignment: CENTER-LEFT; -fx-padding: 12 15; " +
                "-fx-border-width: 0; -fx-background-radius: 0;");
        btn.setCursor(javafx.scene.Cursor.HAND);
        btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; " +
                "-fx-font-size: 13; -fx-alignment: CENTER-LEFT; -fx-padding: 12 15; " +
                "-fx-border-width: 0; -fx-background-radius: 0;"));
        btn.setOnMouseExited(e -> btn.setStyle("-fx-background-color: #2c3e50; -fx-text-fill: white; " +
                "-fx-font-size: 13; -fx-alignment: CENTER-LEFT; -fx-padding: 12 15; " +
                "-fx-border-width: 0; -fx-background-radius: 0;"));
        return btn;
    }

    // ===================== DASHBOARD =====================
    private VBox buatDashboard() {
        VBox panel = new VBox(20);
        panel.setPadding(new Insets(25));
        panel.setStyle("-fx-background-color: #ecf0f1;");

        Label judul = new Label("Dashboard");
        judul.setFont(Font.font("Arial", FontWeight.BOLD, 24));

        GridPane kartu = new GridPane();
        kartu.setHgap(15);
        kartu.setVgap(15);

        kartu.add(buatKartu("Pasien", String.valueOf(daftarPasien.size()), "#3498db"), 0, 0);
        kartu.add(buatKartu("Dokter", String.valueOf(daftarDokter.size()), "#2ecc71"), 1, 0);
        kartu.add(buatKartu("Total Antrean", String.valueOf(antrean.getTotalSemuaAntrean()), "#e67e22"), 2, 0);
        kartu.add(buatKartu("Obat", String.valueOf(daftarObat.size()), "#9b59b6"), 3, 0);
        kartu.add(buatKartu("Rekam Medis", String.valueOf(daftarRekamMedis.size()), "#e74c3c"), 0, 1);
        kartu.add(buatKartu("Jadwal", String.valueOf(daftarJadwal.size()), "#1abc9c"), 1, 1);
        kartu.add(buatKartu("Resep", String.valueOf(daftarResep.size()), "#2980b9"), 2, 1);
        kartu.add(buatKartu("Pembayaran", String.valueOf(daftarPembayaran.size()), "#f39c12"), 3, 1);

        panel.getChildren().addAll(judul, kartu);
        return panel;
    }

    private VBox buatKartu(String judul, String nilai, String warna) {
        VBox kartu = new VBox(8);
        kartu.setAlignment(Pos.CENTER);
        kartu.setPrefSize(160, 100);
        kartu.setStyle("-fx-background-color: " + warna + "; -fx-background-radius: 8;");

        Label lblNilai = new Label(nilai);
        lblNilai.setFont(Font.font("Arial", FontWeight.BOLD, 32));
        lblNilai.setTextFill(Color.WHITE);

        Label lblJudul = new Label(judul);
        lblJudul.setFont(Font.font("Arial", 13));
        lblJudul.setTextFill(Color.web("#ecf0f1"));

        kartu.getChildren().addAll(lblNilai, lblJudul);
        return kartu;
    }

    // ===================== PANEL PASIEN (ADMIN) - UPDATED =====================
    private VBox buatPanelPasien() {
        VBox panel = new VBox(15);
        panel.setPadding(new Insets(25));

        Label judul = new Label("Manajemen Pasien");
        judul.setFont(Font.font("Arial", FontWeight.BOLD, 22));

        TableView<Pasien> tabel = new TableView<>();
        tabel.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(tabel, Priority.ALWAYS);

        TableColumn<Pasien, String> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getIdPasien()));
        
        TableColumn<Pasien, String> colNama = new TableColumn<>("Nama");
        colNama.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getNama()));
        
        TableColumn<Pasien, String> colJK = new TableColumn<>("Jenis Kelamin");
        colJK.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getJenisKelamin()));
        
        TableColumn<Pasien, String> colTgl = new TableColumn<>("Tgl Lahir");
        colTgl.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getTanggalLahir()));
        
        TableColumn<Pasien, String> colTlp = new TableColumn<>("No Telp");
        colTlp.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getNoTelp()));
        
        TableColumn<Pasien, String> colAlamat = new TableColumn<>("Alamat");
        colAlamat.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getAlamat()));

        tabel.getColumns().addAll(colId, colNama, colJK, colTgl, colTlp, colAlamat);
        tabel.setItems(daftarPasien); // ← langsung pakai daftarPasien

        HBox tombol = new HBox(10);
        Button btnTambah = new Button("+ Tambah Pasien");
        Button btnHapus = new Button("Hapus");
        btnTambah.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 15; -fx-background-radius: 5;");
        btnHapus.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 15; -fx-background-radius: 5;");
        Button btnRefresh = new Button("🔄 Refresh");
btnRefresh.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 15; -fx-background-radius: 5;");
btnRefresh.setOnAction(e -> DatabaseHelper.loadPasien(daftarPasien));
        btnTambah.setOnAction(e -> {
    if (daftarDokter.isEmpty()) {
        showAlert("Peringatan", "Belum ada data dokter! Daftarkan dokter terlebih dahulu.");
        return;
    }

    Dialog<Pasien> dialog = new Dialog<>();
    dialog.setTitle("Pendaftaran Pasien & Antrian");
    dialog.setHeaderText("Isi data pasien dan pilih dokter tujuan");
    ButtonType btnSimpan = new ButtonType("Daftarkan", ButtonBar.ButtonData.OK_DONE);
    dialog.getDialogPane().getButtonTypes().addAll(btnSimpan, ButtonType.CANCEL);

    GridPane grid = new GridPane();
    grid.setHgap(10); grid.setVgap(10);
    grid.setPadding(new Insets(20));

    TextField txtNama   = new TextField(); txtNama.setPromptText("Nama lengkap");
    TextField txtAlamat = new TextField(); txtAlamat.setPromptText("Alamat");
    TextField txtTlp    = new TextField(); txtTlp.setPromptText("08xx");
    TextField txtTgl    = new TextField(); txtTgl.setPromptText("dd-mm-yyyy");

    ComboBox<String> cmbJK = new ComboBox<>();
    cmbJK.getItems().addAll("Laki-laki", "Perempuan");
    cmbJK.setValue("Laki-laki");

    ComboBox<Dokter> cmbDokterTujuan = new ComboBox<>();
    cmbDokterTujuan.getItems().addAll(daftarDokter);
    cmbDokterTujuan.setValue(daftarDokter.get(0));

    // Label preview nomor antrian — update otomatis saat pilih dokter
    Label lblNoAntrian = new Label("🎫 No. Antrian: -");
    lblNoAntrian.setFont(Font.font("Arial", FontWeight.BOLD, 16));
    lblNoAntrian.setStyle("-fx-text-fill: #2980b9; -fx-background-color: #eaf4fc; "
            + "-fx-padding: 8 15; -fx-background-radius: 8;");

    Label lblInfoDokter = new Label("");
    lblInfoDokter.setFont(Font.font("Arial", 12));
    lblInfoDokter.setStyle("-fx-text-fill: #555;");

    // Update preview antrian saat dokter dipilih
    cmbDokterTujuan.setOnAction(ev -> {
        Dokter dk = cmbDokterTujuan.getValue();
        if (dk != null) {
            int antriSekarang = antrean.getJumlahAntrean(dk.getIdDokter());
            lblNoAntrian.setText("🎫 No. Antrian: A-" + String.format("%03d", antriSekarang + 1));
            lblInfoDokter.setText("Dokter: " + dk.getNama() + " | " + dk.getSpesialisasi()
                    + " | Antrian saat ini: " + antriSekarang + " orang");
        }
    });

    // Trigger sekali untuk dokter default
    Dokter defaultDokter = daftarDokter.get(0);
    int antriDefault = antrean.getJumlahAntrean(defaultDokter.getIdDokter());
    lblNoAntrian.setText("🎫 No. Antrian: A-" + String.format("%03d", antriDefault + 1));
    lblInfoDokter.setText("Dokter: " + defaultDokter.getNama() + " | " + defaultDokter.getSpesialisasi()
            + " | Antrian saat ini: " + antriDefault + " orang");

    grid.add(new Label("Nama:"),          0, 0); grid.add(txtNama, 1, 0);
    grid.add(new Label("Alamat:"),        0, 1); grid.add(txtAlamat, 1, 1);
    grid.add(new Label("No Telp:"),       0, 2); grid.add(txtTlp, 1, 2);
    grid.add(new Label("Tgl Lahir:"),     0, 3); grid.add(txtTgl, 1, 3);
    grid.add(new Label("Jenis Kelamin:"), 0, 4); grid.add(cmbJK, 1, 4);
    grid.add(new Label("Dokter Tujuan:"), 0, 5); grid.add(cmbDokterTujuan, 1, 5);

    // Separator visual
    grid.add(new Label(""), 0, 6);
    grid.add(lblInfoDokter, 0, 7, 2, 1);
    grid.add(lblNoAntrian,  0, 8, 2, 1);

    dialog.getDialogPane().setContent(grid);
    dialog.getDialogPane().setPrefWidth(420);

    dialog.setResultConverter(btn -> {
        if (btn == btnSimpan && !txtNama.getText().isEmpty()) {
            String id = String.format("P%03d", idPasienCounter++);
            return new Pasien(id, txtNama.getText(), txtAlamat.getText(),
                    txtTlp.getText(), txtTgl.getText(), cmbJK.getValue());
        }
        return null;
    });

    dialog.showAndWait().ifPresent(p -> {
        DatabaseHelper.simpanPasien(p);
        DatabaseHelper.simpanUser(new User(p.getIdPasien(), p.getIdPasien(), "pasien"));

        daftarPasien.add(p);
        users.add(new User(p.getIdPasien(), p.getIdPasien(), "pasien"));

        Dokter drTerpilih = cmbDokterTujuan.getValue();
        String nomorKode = antrean.tambahAntrean(drTerpilih.getIdDokter(), p);
 if (refreshAntreanTabel != null) refreshAntreanTabel.run();

        // Dialog konfirmasi dengan tiket antrian
        Alert tiket = new Alert(Alert.AlertType.INFORMATION);
        tiket.setTitle("✅ Pendaftaran Berhasil");
        tiket.setHeaderText("Pasien berhasil didaftarkan!");
        tiket.setContentText(
            "━━━━━━━━━━━━━━━━━━━━━━\n" +
            "🏥  TIKET ANTRIAN\n" +
            "━━━━━━━━━━━━━━━━━━━━━━\n" +
            "Nama       : " + p.getNama() + "\n" +
            "ID Pasien  : " + p.getIdPasien() + "\n" +
            "Dokter     : " + drTerpilih.getNama() + "\n" +
            "Spesialis  : " + drTerpilih.getSpesialisasi() + "\n" +
            "No. Antrian: " + nomorKode + "\n" +
            "━━━━━━━━━━━━━━━━━━━━━━\n" +
            "Username/Password: " + p.getIdPasien()
        );
        tiket.showAndWait();
    });
});

        btnHapus.setOnAction(e -> {
            Pasien selected = tabel.getSelectionModel().getSelectedItem();
            if (selected != null) {
                daftarPasien.remove(selected); // ← otomatis update tabel
            } else {
                showAlert("Peringatan", "Pilih data pasien terlebih dahulu!");
            }
        });

        tombol.getChildren().addAll(btnTambah, btnHapus, btnRefresh);
        panel.getChildren().addAll(judul, tombol, tabel);
        return panel;
    } 
    // ===================== PANEL DOKTER (ADMIN) - UPDATED =====================
    private VBox buatPanelDokter() {
        VBox panel = new VBox(15);
        panel.setPadding(new Insets(25));
 
        Label judul = new Label("Manajemen Dokter");
        judul.setFont(Font.font("Arial", FontWeight.BOLD, 22));

        // ... Data Dokter Table View ...
        TableView<Dokter> tabel = new TableView<>();
        tabel.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(tabel, Priority.ALWAYS);

        TableColumn<Dokter, String> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getIdDokter()));
        
        TableColumn<Dokter, String> colNama = new TableColumn<>("Nama");
        colNama.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getNama()));
        
        TableColumn<Dokter, String> colSp = new TableColumn<>("Spesialisasi");
        colSp.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getSpesialisasi()));
        
        TableColumn<Dokter, String> colTlp = new TableColumn<>("No Telp");
        colTlp.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getNoTelp()));

        tabel.getColumns().addAll(java.util.Arrays.asList(colId, colNama, colSp, colTlp));
        tabel.setItems(daftarDokter); // ← langsung pakai daftarDokter

HBox tombol = new HBox(10);
Button btnTambah = new Button("+ Tambah Dokter"); // ← fix nama
Button btnHapus = new Button("Hapus");
Button btnRefresh = new Button("🔄 Refresh");
btnRefresh.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 15; -fx-background-radius: 5;");

btnRefresh.setOnAction(e -> {
    DatabaseHelper.loadDokter(daftarDokter); // ← fix ke loadDokter
});
        
        btnTambah.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 15; -fx-background-radius: 5;");
        btnHapus.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 15; -fx-background-radius: 5;");

        btnTambah.setOnAction(e -> {
            Dialog<Dokter> dialog = new Dialog<>();
            dialog.setTitle("Tambah Dokter");
            ButtonType btnSimpan = new ButtonType("Simpan", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(btnSimpan, ButtonType.CANCEL);

            GridPane grid = new GridPane();
            grid.setHgap(10); grid.setVgap(10);
            grid.setPadding(new Insets(20));

            TextField txtNama = new TextField(); txtNama.setPromptText("dr. Nama");
            TextField txtSp = new TextField(); txtSp.setPromptText("Spesialisasi");
            TextField txtTlp = new TextField(); txtTlp.setPromptText("08xx");

            grid.add(new Label("Nama:"), 0, 0); grid.add(txtNama, 1, 0);
            grid.add(new Label("Spesialisasi:"), 0, 1); grid.add(txtSp, 1, 1);
            grid.add(new Label("No Telp:"), 0, 2); grid.add(txtTlp, 1, 2);
            dialog.getDialogPane().setContent(grid);

            dialog.setResultConverter(btn -> {
                if (btn == btnSimpan && !txtNama.getText().isEmpty()) {
                    String id = String.format("D%03d", idDokterCounter++);
                    return new Dokter(id, txtNama.getText(), txtSp.getText(), txtTlp.getText());
                }
                return null;
            });

            dialog.showAndWait().ifPresent(d -> {
    // Simpan ke DB dulu
    DatabaseHelper.simpanDokter(d);
    DatabaseHelper.simpanUser(new User(d.getIdDokter(), d.getIdDokter(), "dokter"));

    daftarDokter.add(d);
    users.add(new User(d.getIdDokter(), d.getIdDokter(), "dokter"));

    showAlert("Sukses Tambah Dokter",
        "Dokter " + d.getNama() + " Berhasil Didaftarkan!\n\n" +
        "Username : " + d.getIdDokter() + "\n" +
        "Password : " + d.getIdDokter()
    );
});
        });

        btnHapus.setOnAction(e -> {
            Dokter selected = tabel.getSelectionModel().getSelectedItem();
            if (selected != null) {
                daftarDokter.remove(selected);
            } else {
                showAlert("Peringatan", "Pilih data dokter terlebih dahulu!");
            }
        });

        tombol.getChildren().addAll(btnTambah, btnHapus, btnRefresh);
        panel.getChildren().addAll(judul, tombol, tabel);
        return panel;
    }

    // ===================== PANEL ANTREAN (ADMIN) - UPDATED =====================
    private VBox buatPanelAntrean() {
        VBox panel = new VBox(15);
        panel.setPadding(new Insets(25));

        Label judul = new Label("Manajemen Antrean per Dokter");
        judul.setFont(Font.font("Arial", FontWeight.BOLD, 22));

        // Selector filter dokter di atas tabel
        HBox filterBox = new HBox(10);
        filterBox.setAlignment(Pos.CENTER_LEFT);
        Label lblPilihDokter = new Label("Pilih Tampilan Antrean Dokter:");
        lblPilihDokter.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        ComboBox<Dokter> cmbFilterDokter = new ComboBox<>();
        cmbFilterDokter.getItems().addAll(daftarDokter);
        
        Label lblJumlah = new Label("Jumlah antrean: 0 orang");
        lblJumlah.setFont(Font.font("Arial", 14));

        TableView<String[]> tabel = new TableView<>();
        tabel.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(tabel, Priority.ALWAYS);
        
        TableColumn<String[], String> colNo = new TableColumn<>("Kode Antrean");
        colNo.setCellValueFactory((TableColumn.CellDataFeatures<String[], String> d) -> 
            new SimpleStringProperty(d.getValue()[0])
        );

        TableColumn<String[], String> colId = new TableColumn<>("ID Pasien");
        colId.setCellValueFactory((TableColumn.CellDataFeatures<String[], String> d) -> 
            new SimpleStringProperty(d.getValue()[1])
        );

        TableColumn<String[], String> colNama = new TableColumn<>("Nama Pasien");
        colNama.setCellValueFactory((TableColumn.CellDataFeatures<String[], String> d) -> 
            new SimpleStringProperty(d.getValue()[2])
        );

        tabel.getColumns().addAll(colNo, colId, colNama);
        ObservableList<String[]> dataTabel = FXCollections.observableArrayList();
        tabel.setItems(dataTabel);

        refreshAntreanTabel = () -> {
            dataTabel.clear();
            Dokter dok = cmbFilterDokter.getValue();
            if (dok != null) {
                lblJumlah.setText("Jumlah antrean: " + antrean.getJumlahAntrean(dok.getIdDokter()) + " orang");
                dataTabel.addAll(antrean.getDaftarAntreanString(dok.getIdDokter()));
            }
        };

        cmbFilterDokter.setOnAction(e -> refreshAntreanTabel.run());
if (!daftarDokter.isEmpty()) {
    cmbFilterDokter.setValue(daftarDokter.get(0));
    refreshAntreanTabel.run();
}
javafx.animation.Timeline autoRefresh = new javafx.animation.Timeline(
    new javafx.animation.KeyFrame(
        javafx.util.Duration.seconds(2),
        ev -> refreshAntreanTabel.run()
    )
);
autoRefresh.setCycleCount(javafx.animation.Animation.INDEFINITE);
autoRefresh.play();

        filterBox.getChildren().addAll(lblPilihDokter, cmbFilterDokter, lblJumlah);

        // Hanya tersisa Tombol Panggil Berikutnya
        HBox tombol = new HBox(10);
        Button btnPanggil = new Button("Panggil Berikutnya");
        btnPanggil.setStyle("-fx-background-color: #e67e22; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 15; -fx-background-radius: 5;");
        Button btnRefresh = new Button("🔄 Refresh");
btnRefresh.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 15; -fx-background-radius: 5;");
btnRefresh.setOnAction(e -> refreshAntreanTabel.run());
        btnPanggil.setOnAction(e -> {
            Dokter dokTerpilih = cmbFilterDokter.getValue();
            if (dokTerpilih != null) {
                Pasien p = antrean.panggilBerikutnya(dokTerpilih.getIdDokter());
                if (p != null) {
                    refreshAntreanTabel.run();
                    showAlert("Panggilan", "Memanggil Pasien: \n" + p.getNama() + "\nMenuju Ruang " + dokTerpilih.getNama());
                } else {
                    showAlert("Info", "Antrean untuk " + dokTerpilih.getNama() + " sudah kosong!");
                }
            } else {
                showAlert("Info", "Pilih dokter terlebih dahulu!");
            }
        });

        tombol.getChildren().addAll(btnPanggil);
        panel.getChildren().addAll(judul, filterBox, tombol, tabel);
        return panel;
    }

    // ===================== PANEL OBAT (ADMIN) =====================
    private VBox buatPanelObat() {
        VBox panel = new VBox(15);
        panel.setPadding(new Insets(25));

        Label judul = new Label("Manajemen Obat");
        judul.setFont(Font.font("Arial", FontWeight.BOLD, 22));

        TableView<Obat> tabel = new TableView<>();
        tabel.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(tabel, Priority.ALWAYS);

        TableColumn<Obat, String> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getIdObat()));
        
        TableColumn<Obat, String> colNama = new TableColumn<>("Nama Obat");
        colNama.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getNamaObat()));
        
        TableColumn<Obat, String> colJenis = new TableColumn<>("Jenis");
        colJenis.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getJenis()));
        
        TableColumn<Obat, Integer> colStok = new TableColumn<>("Stok");
        colStok.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getStok()));
        
        TableColumn<Obat, Double> colHarga = new TableColumn<>("Harga");
        colHarga.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getHarga()));

        tabel.getColumns().addAll(colId, colNama, colJenis, colStok, colHarga);
        ObservableList<Obat> data = FXCollections.observableArrayList(daftarObat);
        tabel.setItems(data);

        HBox tombol = new HBox(10);
        Button btnTambah = new Button("+ Tambah Obat");
        Button btnHapus = new Button("Hapus");
        btnTambah.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 15; -fx-background-radius: 5;");
        btnHapus.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 15; -fx-background-radius: 5;");
        Button btnRefresh = new Button("🔄 Refresh");
btnRefresh.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 15; -fx-background-radius: 5;");
btnRefresh.setOnAction(e -> DatabaseHelper.loadObat(daftarObat));
        btnTambah.setOnAction(e -> {
            Dialog<Obat> dialog = new Dialog<>();
            dialog.setTitle("Tambah Obat");
            ButtonType btnSimpan = new ButtonType("Simpan", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(btnSimpan, ButtonType.CANCEL);

            GridPane grid = new GridPane();
            grid.setHgap(10); grid.setVgap(10);
            grid.setPadding(new Insets(20));

            TextField txtNama = new TextField();
            TextField txtJenis = new TextField();
            TextField txtStok = new TextField();
            TextField txtHarga = new TextField();

            grid.add(new Label("Nama Obat:"), 0, 0); grid.add(txtNama, 1, 0);
            grid.add(new Label("Jenis:"), 0, 1); grid.add(txtJenis, 1, 1);
            grid.add(new Label("Stok:"), 0, 2); grid.add(txtStok, 1, 2);
            grid.add(new Label("Harga:"), 0, 3); grid.add(txtHarga, 1, 3);
            dialog.getDialogPane().setContent(grid);

            dialog.setResultConverter(btn -> {
                if (btn == btnSimpan && !txtNama.getText().isEmpty()) {
                    String id = String.format("O%03d", idObatCounter++);
                    return new Obat(id, txtNama.getText(), txtJenis.getText(),
                            Integer.parseInt(txtStok.getText()), Double.parseDouble(txtHarga.getText()));
                }
                return null;
            });

            dialog.showAndWait().ifPresent(o -> { daftarObat.add(o); data.add(o); });
        });

        btnHapus.setOnAction(e -> {
            Obat selected = tabel.getSelectionModel().getSelectedItem();
            if (selected != null) {
                daftarObat.remove(selected);
                data.remove(selected);
            } else {
                showAlert("Peringatan", "Pilih data obat terlebih dahulu!");
            }
        });

        tombol.getChildren().addAll(btnTambah, btnHapus, btnRefresh);
        panel.getChildren().addAll(judul, tombol, tabel);
        return panel;
    }

    // ===================== PANEL JADWAL (ADMIN) =====================
    private VBox buatPanelJadwal() {
        VBox panel = new VBox(15);
        panel.setPadding(new Insets(25));

        Label judul = new Label("Jadwal Dokter");
        judul.setFont(Font.font("Arial", FontWeight.BOLD, 22));

        TableView<JadwalDokter> tabel = new TableView<>();
        tabel.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(tabel, Priority.ALWAYS);

        TableColumn<JadwalDokter, String> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getIdJadwal()));

        TableColumn<JadwalDokter, String> colDokter = new TableColumn<>("Nama Dokter");
        colDokter.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDokter().getNama()));
        
        TableColumn<JadwalDokter, String> colHari = new TableColumn<>("Hari");
        colHari.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getHari()));
        
        TableColumn<JadwalDokter, String> colMulai = new TableColumn<>("Jam Mulai");
        colMulai.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getJamMulai()));
        
        TableColumn<JadwalDokter, String> colSelesai = new TableColumn<>("Jam Selesai");
        colSelesai.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getJamSelesai()));
        
        TableColumn<JadwalDokter, Integer> colKuota = new TableColumn<>("Kuota");
        colKuota.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getKuota()));

        tabel.getColumns().addAll(colId, colDokter, colHari, colMulai, colSelesai, colKuota);
        ObservableList<JadwalDokter> data = FXCollections.observableArrayList(daftarJadwal);
        tabel.setItems(data);

        HBox tombol = new HBox(10);
        Button btnTambah = new Button("+ Tambah Jadwal");
        Button btnHapus = new Button("Hapus");
        Button btnRefresh = new Button("🔄 Refresh");
        btnRefresh.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold;");
        btnTambah.setStyle("-fx-background-color: #1abc9c; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 15; -fx-background-radius: 5;");
        btnHapus.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 15; -fx-background-radius: 5;");

        btnTambah.setOnAction(e -> {
            if (daftarDokter.isEmpty()) { showAlert("Error", "Belum ada dokter!"); return; }
            Dialog<JadwalDokter> dialog = new Dialog<>();
            dialog.setTitle("Tambah Jadwal");
            ButtonType btnSimpan = new ButtonType("Simpan", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(btnSimpan, ButtonType.CANCEL);

            GridPane grid = new GridPane();
            grid.setHgap(10); grid.setVgap(10);
            grid.setPadding(new Insets(20));

            ComboBox<Dokter> cmbDokter = new ComboBox<>();
            cmbDokter.getItems().addAll(daftarDokter);
            cmbDokter.setValue(daftarDokter.get(0));
            ComboBox<String> cmbHari = new ComboBox<>();
            cmbHari.getItems().addAll("Senin","Selasa","Rabu","Kamis","Jumat","Sabtu");
            cmbHari.setValue("Senin");
            TextField txtMulai = new TextField("08:00");
            TextField txtSelesai = new TextField("12:00");
            TextField txtKuota = new TextField("20");

            grid.add(new Label("Dokter:"), 0, 0); grid.add(cmbDokter, 1, 0);
            grid.add(new Label("Hari:"), 0, 1); grid.add(cmbHari, 1, 1);
            grid.add(new Label("Jam Mulai:"), 0, 2); grid.add(txtMulai, 1, 2);
            grid.add(new Label("Jam Selesai:"), 0, 3); grid.add(txtSelesai, 1, 3);
            grid.add(new Label("Kuota:"), 0, 4); grid.add(txtKuota, 1, 4);
            dialog.getDialogPane().setContent(grid);

            dialog.setResultConverter(btn -> {
                if (btn == btnSimpan) {
                    Dokter d = cmbDokter.getValue();
                    String id = String.format("J%03d", idJadwalCounter++);
                    return new JadwalDokter(id, d, cmbHari.getValue(),
                            txtMulai.getText(), txtSelesai.getText(), Integer.parseInt(txtKuota.getText()));
                }
                return null;
            });

            dialog.showAndWait().ifPresent(j -> { daftarJadwal.add(j); data.add(j); });
        });

        btnHapus.setOnAction(e -> {
            JadwalDokter selected = tabel.getSelectionModel().getSelectedItem();
            if (selected != null) {
                daftarJadwal.remove(selected);
                data.remove(selected);
            } else {
                showAlert("Peringatan", "Pilih data jadwal terlebih dahulu!");
            }
        });

        tombol.getChildren().addAll(btnTambah, btnHapus, btnRefresh);
        panel.getChildren().addAll(judul, tombol, tabel);
        return panel;
    }

    // ===================== PANEL PEMBAYARAN (ADMIN) =====================
    private VBox buatPanelPembayaran() {
        VBox panel = new VBox(15);
        panel.setPadding(new Insets(25));

        Label judul = new Label("Pembayaran");
        judul.setFont(Font.font("Arial", FontWeight.BOLD, 22));

        TableView<Pembayaran> tabel = new TableView<>();
        tabel.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(tabel, Priority.ALWAYS);

        TableColumn<Pembayaran, String> colId = new TableColumn<>("ID Bayar");
        colId.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getIdPembayaran()));
        TableColumn<Pembayaran, String> colTgl = new TableColumn<>("Tanggal");
        colTgl.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getTanggal()));
        TableColumn<Pembayaran, Double> colTotal = new TableColumn<>("Total (Rp)");
        colTotal.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getTotalBayar()));
        TableColumn<Pembayaran, String> colStatus = new TableColumn<>("Status");
        colStatus.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getStatus()));

        tabel.getColumns().addAll(colId, colTgl, colTotal, colStatus);
        ObservableList<Pembayaran> data = FXCollections.observableArrayList(daftarPembayaran);
        tabel.setItems(data);

        HBox tombol = new HBox(10);
        Button btnProses = new Button("Proses Pembayaran");
        Button btnLunas = new Button("Tandai Lunas");
        Button btnRefresh = new Button("🔄 Refresh");
        btnRefresh.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold;");
        btnProses.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 15; -fx-background-radius: 5;");
        btnLunas.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 15; -fx-background-radius: 5;");

        btnProses.setOnAction(e -> {
            if (daftarRekamMedis.isEmpty()) { showAlert("Error", "Belum ada rekam medis!"); return; }
            Dialog<Pembayaran> dialog = new Dialog<>();
            dialog.setTitle("Proses Pembayaran");
            ButtonType btnSimpan = new ButtonType("Proses", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(btnSimpan, ButtonType.CANCEL);

            GridPane grid = new GridPane();
            grid.setHgap(10); grid.setVgap(10);
            grid.setPadding(new Insets(20));

            ComboBox<String> cmbRM = new ComboBox<>();
            for (RekamMedis rm : daftarRekamMedis)
                cmbRM.getItems().add(rm.getIdRekamMedis() + " - " + rm.getPasien().getNama());
            cmbRM.setValue(cmbRM.getItems().get(0));
            TextField txtBiaya = new TextField("50000");

            grid.add(new Label("Rekam Medis:"), 0, 0); grid.add(cmbRM, 1, 0);
            grid.add(new Label("Biaya Konsultasi (Rp):"), 0, 1); grid.add(txtBiaya, 1, 1);
            dialog.getDialogPane().setContent(grid);

            dialog.setResultConverter(btn -> {
                if (btn == btnSimpan) {
                    Region r = null;
                    RekamMedis rm = daftarRekamMedis.get(cmbRM.getSelectionModel().getSelectedIndex());
                    String id = String.format("PAY%03d", idBayarCounter++);
                    return new Pembayaran(id, rm, Double.parseDouble(txtBiaya.getText()),
                            java.time.LocalDate.now().toString());
                }
                return null;
            });

            dialog.showAndWait().ifPresent(p -> { daftarPembayaran.add(p); data.add(p); });
        });

        btnLunas.setOnAction(e -> {
            Pembayaran selected = tabel.getSelectionModel().getSelectedItem();
            if (selected != null) { selected.bayar(); tabel.refresh(); }
        });

        tombol.getChildren().addAll(btnProses, btnLunas);
        panel.getChildren().addAll(judul, tombol, tabel);
        return panel;
    }

    private VBox buatPanelRekamMedis(boolean readOnly) {
        VBox panel = new VBox(15); panel.setPadding(new Insets(25));
        Label judul = new Label("Rekam Medis");
        judul.setFont(Font.font("Arial", FontWeight.BOLD, 22));
        TableView<RekamMedis> tabel = new TableView<>();
        tabel.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY); VBox.setVgrow(tabel, Priority.ALWAYS);
        TableColumn<RekamMedis, String> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getIdRekamMedis()));
        TableColumn<RekamMedis, String> colTgl = new TableColumn<>("Tanggal");
        colTgl.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getTanggal()));
        TableColumn<RekamMedis, String> colKeluhan = new TableColumn<>("Keluhan");
        colKeluhan.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getKeluhan()));
        TableColumn<RekamMedis, String> colDiagnosis = new TableColumn<>("Diagnosis");
        colDiagnosis.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDiagnosis()));
        tabel.getColumns().addAll(colId, colTgl, colKeluhan, colDiagnosis);
        ObservableList<RekamMedis> data = FXCollections.observableArrayList(daftarRekamMedis);
        tabel.setItems(data);
        panel.getChildren().addAll(judul, tabel);
        if (!readOnly) {
            HBox tombol = new HBox(10);
            Button btnTambah = new Button("+ Input Rekam Medis");
            btnTambah.setStyle("-fx-background-color: #9b59b6; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 15; -fx-background-radius: 5;");
            btnTambah.setOnAction(e -> {
                if (daftarPasien.isEmpty()) return;
                Dialog<RekamMedis> dialog = new Dialog<>(); dialog.setTitle("Input Rekam Medis");
                ButtonType btnSimpan = new ButtonType("Simpan", ButtonBar.ButtonData.OK_DONE);
                dialog.getDialogPane().getButtonTypes().addAll(btnSimpan, ButtonType.CANCEL);
                GridPane grid = new GridPane(); grid.setHgap(10); grid.setVgap(10); grid.setPadding(new Insets(20));
                ComboBox<Pasien> cmbPasien = new ComboBox<>(); cmbPasien.getItems().addAll(daftarPasien); cmbPasien.setValue(daftarPasien.get(0));
                TextField txtKeluhan = new TextField(); TextField txtDiagnosis = new TextField();
                grid.add(new Label("Pasien:"), 0, 0); grid.add(cmbPasien, 1, 0);
                grid.add(new Label("Keluhan:"), 0, 1); grid.add(txtKeluhan, 1, 1);
                grid.add(new Label("Diagnosis:"), 0, 2); grid.add(txtDiagnosis, 1, 2);
                dialog.getDialogPane().setContent(grid);
                dialog.setResultConverter(b -> b == btnSimpan ? new RekamMedis(String.format("RM%03d", idRMCounter++), cmbPasien.getValue(), dokterLogin, java.time.LocalDate.now().toString(), txtKeluhan.getText(), txtDiagnosis.getText()) : null);
                dialog.showAndWait().ifPresent(rm -> { daftarRekamMedis.add(rm); data.add(rm); });
            });
            Button btnRefresh = new Button("🔄 Refresh");
btnRefresh.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 15; -fx-background-radius: 5;");
btnRefresh.setOnAction(e -> {
    DatabaseHelper.loadPasien(daftarPasien);
    DatabaseHelper.loadDokter(daftarDokter);
});
            tombol.getChildren().add(btnTambah); tombol.getChildren().add(btnRefresh); panel.getChildren().add(1, tombol);
        }
        return panel;
    }

    private VBox buatPanelResep(boolean readOnly) {
        VBox panel = new VBox(15); panel.setPadding(new Insets(25));
        Label judul = new Label("Resep Obat"); judul.setFont(Font.font("Arial", FontWeight.BOLD, 22));
        TableView<Resep> tabel = new TableView<>(); tabel.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY); VBox.setVgrow(tabel, Priority.ALWAYS);
        TableColumn<Resep, String> colId = new TableColumn<>("ID Resep"); colId.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getIdResep()));
        TableColumn<Resep, String> colObat = new TableColumn<>("Nama Obat"); colObat.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getObat().getNamaObat()));
        tabel.getColumns().addAll(colId, colObat); ObservableList<Resep> data = FXCollections.observableArrayList(daftarResep); tabel.setItems(data);
        panel.getChildren().addAll(judul, tabel); return panel;
    }

    // ===================== DASHBOARD AKUN PASIEN (NOMOR ANTREAN) - UPDATED =====================
    private VBox buatPanelNomorAntrean() {
        VBox panel = new VBox(20); 
        panel.setPadding(new Insets(25)); 
        panel.setAlignment(Pos.TOP_CENTER);
        
        Label judul = new Label("Status Nomor Antrean Anda"); 
        judul.setFont(Font.font("Arial", FontWeight.BOLD, 22));
        
        Label lblNama = new Label("Selamat Datang, " + (pasienLogin != null ? pasienLogin.getNama() : "-") + "!"); 
        lblNama.setFont(Font.font("Arial", FontWeight.BOLD, 16));

        GridPane infoAntrean = new GridPane();
        infoAntrean.setHgap(15); infoAntrean.setVgap(15);
        infoAntrean.setAlignment(Pos.CENTER);
        infoAntrean.setStyle("-fx-background-color: #f8f9fa; -fx-padding: 25; -fx-background-radius: 10; -fx-border-color: #dee2e6; -fx-border-radius: 10;");

        Label valDr = new Label(); valDr.setStyle("-fx-font-weight: bold;");
        Label valNoSaya = new Label(); valNoSaya.setStyle("-fx-font-weight: bold; -fx-text-fill: #2ecc71; -fx-font-size: 18;");
        Label valSisa = new Label(); valSisa.setStyle("-fx-font-weight: bold; -fx-text-fill: #e67e22; -fx-font-size: 14;");
        Label valSedangPanggil = new Label(); valSedangPanggil.setStyle("-fx-font-weight: bold; -fx-text-fill: #e74c3c; -fx-font-size: 14;");
        Label lblPesanKosong = new Label("Anda belum terdaftar di antrean dokter manapun saat ini.");
        lblPesanKosong.setFont(Font.font("Arial", 14));

        // Fungsi refresh status pelacakan antrean pasien
        refreshDashboard = () -> {
            infoAntrean.getChildren().clear();
            if (pasienLogin == null) {
                infoAntrean.add(lblPesanKosong, 0, 0);
                return;
            }
            
            Dokter drTujuan = null;
            int sisaDidepan = -1;
            
            // Mencari dokter mana yang sedang memproses antrean pasien saat ini
            for (Dokter d : daftarDokter) {
                int sisa = antrean.getSisaDidepan(d.getIdDokter(), pasienLogin);
                if (sisa != -1) {
                    drTujuan = d;
                    sisaDidepan = sisa;
                    break;
                }
            }

            if (drTujuan != null) {
                valDr.setText(drTujuan.getNama());
                valNoSaya.setText(antrean.getKodeAntrean(pasienLogin));
                valSisa.setText(sisaDidepan == 0 ? "Giliran Anda Berikutnya! Silakan Menuju Ruangan." : sisaDidepan + " Orang Lagi");
                valSedangPanggil.setText(antrean.getSedangDitangani(drTujuan.getIdDokter()));

                infoAntrean.add(new Label("Dokter Tujuan:"), 0, 0); infoAntrean.add(valDr, 1, 0);
                infoAntrean.add(new Label("Nomor Antrean Anda:"), 0, 1); infoAntrean.add(valNoSaya, 1, 1);
                infoAntrean.add(new Label("Sisa Antrean di Depan:"), 0, 2); infoAntrean.add(valSisa, 1, 2);
                infoAntrean.add(new Label("Antrean Sedang Ditangani:"), 0, 3); infoAntrean.add(valSedangPanggil, 1, 3);
            } else {
                infoAntrean.add(lblPesanKosong, 0, 0);
            }
        };

        // Jalankan pelacakan data saat halaman dibuka pertama kali
        refreshDashboard.run();

        Button btnRefresh = new Button("🔄 Refresh Status Antrean");
        btnRefresh.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 15; -fx-background-radius: 5;");
        btnRefresh.setOnAction(e -> refreshDashboard.run());

        panel.getChildren().addAll(judul, lblNama, infoAntrean, btnRefresh); 
        return panel;
    }

    private VBox buatPanelRekamMedisPribadi() {
        VBox panel = new VBox(15); panel.setPadding(new Insets(25));
        Label judul = new Label("Rekam Medis Pribadi"); judul.setFont(Font.font("Arial", FontWeight.BOLD, 22));
        TableView<RekamMedis> tabel = new TableView<>(); tabel.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY); VBox.setVgrow(tabel, Priority.ALWAYS);
        TableColumn<RekamMedis, String> colId = new TableColumn<>("ID"); colId.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getIdRekamMedis()));
        tabel.getColumns().add(colId); ObservableList<RekamMedis> data = FXCollections.observableArrayList(); tabel.setItems(data);
        panel.getChildren().addAll(judul, tabel); return panel;
    }

    private VBox buatPanelResepPasien() {
        VBox panel = new VBox(15); panel.setPadding(new Insets(25));
        Label judul = new Label("Resep Obat Saya"); judul.setFont(Font.font("Arial", FontWeight.BOLD, 22));
        TableView<Resep> tabel = new TableView<>(); tabel.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY); VBox.setVgrow(tabel, Priority.ALWAYS);
        TableColumn<Resep, String> colId = new TableColumn<>("ID Resep"); colId.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getIdResep()));
        tabel.getColumns().add(colId); ObservableList<Resep> data = FXCollections.observableArrayList(); tabel.setItems(data);
        panel.getChildren().addAll(judul, tabel); return panel;
    }

    private void showAlert(String judul, String pesan) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(judul); alert.setHeaderText(null); alert.setContentText(pesan); alert.showAndWait();
    }
}

// =========================================================================
//  KELAS-KELAS MODEL / PENDUKUNG (UPDATED ANTREAN & TRACKING LOGIC)
// =========================================================================

class User {
    private String username; private String password; private String role;
    public User(String username, String password, String role) { this.username = username; this.password = password; this.role = role; }
    public boolean login(String u, String p) { return this.username.equals(u) && this.password.equals(p); }
    public String getUsername() { return username; } public String getRole() { return role; }
    public String getPassword() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getPassword'");
    }
}

class Pasien {
    private String idPasien; private String nama; private String alamat; private String noTelp; private String tanggalLahir; private String jenisKelamin;
    public Pasien(String idPasien, String nama, String alamat, String noTelp, String tanggalLahir, String jenisKelamin) { this.idPasien = idPasien; this.nama = nama; this.alamat = alamat; this.noTelp = noTelp; this.tanggalLahir = tanggalLahir; this.jenisKelamin = jenisKelamin; }
    public String getIdPasien() { return idPasien; } public String getNama() { return nama; } public String getAlamat() { return alamat; } public String getNoTelp() { return noTelp; } public String getTanggalLahir() { return tanggalLahir; } public String getJenisKelamin() { return jenisKelamin; }
    @Override public String toString() { return nama + " (" + idPasien + ")"; }
}

class Dokter {
    private String idDokter; private String nama; private String spesialisasi; private String noTelp;
    public Dokter(String idDokter, String nama, String spesialisasi, String noTelp) { this.idDokter = idDokter; this.nama = nama; this.spesialisasi = spesialisasi; this.noTelp = noTelp; }
    public String getIdDokter() { return idDokter; } public String getNama() { return nama; } public String getSpesialisasi() { return spesialisasi; } public String getNoTelp() { return noTelp; }
    @Override public String toString() { return nama + " [" + spesialisasi + "]"; }
}

class Antrean {
    private Map<String, Queue<Pasien>> mapQueue = new HashMap<>();
    private Map<String, Integer> mapCounter = new HashMap<>();
    private Map<Pasien, String> mapKodeAntrean = new HashMap<>();
    
    // Melacak nomor kode antrean yang saat ini sedang dipanggil/ditangani per Dokter
    private Map<String, String> mapSedangDitangani = new HashMap<>();

    public String tambahAntrean(String idDokter, Pasien p) {
        mapQueue.putIfAbsent(idDokter, new LinkedList<>());
        mapCounter.putIfAbsent(idDokter, 0);

        int counterTerbaru = mapCounter.get(idDokter) + 1;
        mapCounter.put(idDokter, counterTerbaru);

        String kodeAntrean = String.format("A-%03d", counterTerbaru);
        
        mapQueue.get(idDokter).add(p);
        mapKodeAntrean.put(p, kodeAntrean);
        
        return kodeAntrean;
    }

    public void inisialisasiCounter(String idDokter, int sudahAda) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'inisialisasiCounter'");
    }

    public Pasien panggilBerikutnya(String idDokter) {
        Queue<Pasien> q = mapQueue.get(idDokter);
        if (q != null && !q.isEmpty()) {
            Pasien p = q.poll();
            String kode = mapKodeAntrean.get(p);
            
            // Simpan status nomor antrean yang sedang aktif ditangani
            mapSedangDitangani.put(idDokter, kode);
            return p;
        }
        return null;
    }

    public int getJumlahAntrean(String idDokter) {
        Queue<Pasien> q = mapQueue.get(idDokter);
        return q != null ? q.size() : 0;
    }

    public int getTotalSemuaAntrean() {
        int total = 0;
        for (Queue<Pasien> q : mapQueue.values()) {
            total += q.size();
        }
        return total;
    }

    public ArrayList<String[]> getDaftarAntreanString(String idDokter) {
        ArrayList<String[]> daftar = new ArrayList<>();
        Queue<Pasien> q = mapQueue.get(idDokter);
        if (q != null) {
            for (Pasien p : q) {
                String kode = mapKodeAntrean.getOrDefault(p, "A-000");
                daftar.add(new String[]{kode, p.getIdPasien(), p.getNama()});
            }
        }
        return daftar;
    }

    // Mengambil nomor urut antrean milik pasien tertentu
    public String getKodeAntrean(Pasien p) {
        return mapKodeAntrean.getOrDefault(p, "-");
    }

    // Mengambil nomor antrean yang sedang diperiksa oleh dokter
    public String getSedangDitangani(String idDokter) {
        return mapSedangDitangani.getOrDefault(idDokter, "Belum ada");
    }

    // Menghitung jumlah pasien tersisa di depan barisan antrean pasien tertentu
    public int getSisaDidepan(String idDokter, Pasien p) {
        Queue<Pasien> q = mapQueue.get(idDokter);
        if (q == null) return -1;
        
        int count = 0;
        for (Pasien pasienDiDalamQueue : q) {
            if (pasienDiDalamQueue.getIdPasien().equals(p.getIdPasien())) {
                return count; // Berhasil ketemu sisa orang di depannya
            }
            count++;
        }
        return -1; // Pasien tidak ditemukan di antrean aktif dokter ini
    }
}

class Obat {
    private String idObat; private String namaObat; private String jenis; private int stok; private double harga;
    public Obat(String idObat, String namaObat, String jenis, int stok, double harga) { this.idObat = idObat; this.namaObat = namaObat; this.jenis = jenis; this.stok = stok; this.harga = harga; }
    public String getIdObat() { return idObat; } public String getNamaObat() { return namaObat; } public String getJenis() { return jenis; } public int getStok() { return stok; } public double getHarga() { return harga; }
}

class JadwalDokter {
    private String idJadwal; private Dokter dokter; private String hari; private String jamMulai; private String jamSelesai; private int kuota;
    public JadwalDokter(String idJadwal, Dokter dokter, String hari, String jamMulai, String jamSelesai, int kuota) { this.idJadwal = idJadwal; this.dokter = dokter; this.hari = hari; this.jamMulai = jamMulai; this.jamSelesai = jamSelesai; this.kuota = kuota; }
    public String getIdJadwal() { return idJadwal; } public Dokter getDokter() { return dokter; } public String getHari() { return hari; } public String getJamMulai() { return jamMulai; } public String getJamSelesai() { return jamSelesai; } public int getKuota() { return kuota; }
}

class RekamMedis {
    private String idRekamMedis; private Pasien pasien; private Dokter dokter; private String tanggal; private String keluhan; private String diagnosis;
    public RekamMedis(String idRekamMedis, Pasien pasien, Dokter dokter, String tanggal, String keluhan, String diagnosis) { this.idRekamMedis = idRekamMedis; this.pasien = pasien; this.dokter = dokter; this.tanggal = tanggal; this.keluhan = keluhan; this.diagnosis = diagnosis; }
    public String getIdRekamMedis() { return idRekamMedis; } public Pasien getPasien() { return pasien; } public Dokter getDokter() { return dokter; } public String getTanggal() { return tanggal; } public String getKeluhan() { return keluhan; } public String getDiagnosis() { return diagnosis; }
    public double getTotalBiayaObat() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getTotalBiayaObat'");
    }
}

class Resep {
    private String idResep; private Obat obat; private int jumlah; private String aturanPakai;
    public Resep(String idResep, Obat obat, int jumlah, String aturanPakai) { this.idResep = idResep; this.obat = obat; this.jumlah = jumlah; this.aturanPakai = aturanPakai; }
    public String getIdResep() { return idResep; } public Obat getObat() { return obat; } public int getJumlah() { return jumlah; } public String getAturanPakai() { return aturanPakai; }
    public double getTotalHarga() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getTotalHarga'");
    }
}

class Pembayaran {
    private String idPembayaran; private RekamMedis rekamMedis; private double totalBayar; private String tanggal; private String status;
    public Pembayaran(String idPembayaran, RekamMedis rekamMedis, double totalBayar, String tanggal) { this.idPembayaran = idPembayaran; this.rekamMedis = rekamMedis; this.totalBayar = totalBayar; this.tanggal = tanggal; this.status = "Belum Lunas"; }
    public void bayar() { this.status = "Lunas"; } public String getIdPembayaran() { return idPembayaran; } public RekamMedis getRekamMedis() { return rekamMedis; } public double getTotalBayar() { return totalBayar; } public String getTanggal() { return tanggal; } public String getStatus() { return status; }
	public void setStatus(String string) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'setStatus'");
	}
}