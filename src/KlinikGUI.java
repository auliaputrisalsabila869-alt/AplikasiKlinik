import java.sql.*;
import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
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
    private ArrayList<Pasien> daftarPasien = new ArrayList<>();
    private ArrayList<Dokter> daftarDokter = new ArrayList<>();
    private ArrayList<Obat> daftarObat = new ArrayList<>();
    private ArrayList<JadwalDokter> daftarJadwal = new ArrayList<>();
    private ArrayList<RekamMedis> daftarRekamMedis = new ArrayList<>();
    private ArrayList<Resep> daftarResep = new ArrayList<>();
    private ArrayList<Pembayaran> daftarPembayaran = new ArrayList<>();
    private Antrean antrean = new Antrean();

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
    public void start(Stage stage) {
        this.primaryStage = stage;
        initDataAwal();
        showLogin();
    }

    // ===================== DATA AWAL =====================
    private void initDataAwal() {
        try (Connection conn = DatabaseHelper.getConnection()) {
            ResultSet rsU = conn.createStatement().executeQuery("SELECT * FROM users");
            while (rsU.next())
                users.add(new User(rsU.getString("username"), rsU.getString("password"), rsU.getString("role")));

            ResultSet rsD = conn.createStatement().executeQuery("SELECT * FROM dokter");
            while (rsD.next())
                daftarDokter.add(new Dokter(rsD.getString("id_dokter"), rsD.getString("nama"),
                    rsD.getString("spesialisasi"), rsD.getString("no_telp")));

            for (Dokter d : daftarDokter) {
                int sudahAda = DatabaseHelper.getJumlahAntreanDariDB(d.getIdDokter());
                antrean.inisialisasiCounter(d.getIdDokter(), sudahAda);
            }

            ResultSet rsP = conn.createStatement().executeQuery("SELECT * FROM pasien");
            while (rsP.next())
                daftarPasien.add(new Pasien(rsP.getString("id_pasien"), rsP.getString("nama"),
                    rsP.getString("alamat"), rsP.getString("no_telp"),
                    rsP.getString("tanggal_lahir"), rsP.getString("jenis_kelamin")));

            ResultSet rsO = conn.createStatement().executeQuery("SELECT * FROM obat");
            while (rsO.next())
                daftarObat.add(new Obat(rsO.getString("id_obat"), rsO.getString("nama"),
                    rsO.getString("kategori"), rsO.getInt("stok"), rsO.getInt("harga")));

            ResultSet rsJ = conn.createStatement().executeQuery(
                "SELECT j.*, d.nama, d.spesialisasi, d.no_telp FROM jadwal_dokter j JOIN dokter d ON j.id_dokter = d.id_dokter");
            while (rsJ.next()) {
                Dokter dk = new Dokter(rsJ.getString("id_dokter"), rsJ.getString("nama"),
                    rsJ.getString("spesialisasi"), rsJ.getString("no_telp"));
                daftarJadwal.add(new JadwalDokter(rsJ.getString("id_jadwal"), dk,
                    rsJ.getString("hari"), rsJ.getString("jam_mulai"),
                    rsJ.getString("jam_selesai"), rsJ.getInt("kuota")));
            }

            idPasienCounter = daftarPasien.size() + 1;
            idDokterCounter = daftarDokter.size() + 1;
            idObatCounter = daftarObat.size() + 1;
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
                            if (ps.getIdPasien().equals(u)) { pasienLogin = ps; break; }
                        }
                    } else if (usr.getRole().equals("dokter")) {
                        for (Dokter dk : daftarDokter) {
                            if (dk.getIdDokter().equalsIgnoreCase(u)) { dokterLogin = dk; break; }
                        }
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

            if (u.toLowerCase().startsWith("dokter") && p.equals("dok123")) {
                String angka = u.substring(6);
                try {
                    int nomor = Integer.parseInt(angka);
                    String idDokter = String.format("D%03d", nomor);
                    for (Dokter dk : daftarDokter) {
                        if (dk.getIdDokter().equalsIgnoreCase(idDokter)) {
                            userLogin = new User(u, p, "dokter");
                            pasienLogin = null;
                            dokterLogin = dk;
                            showMain();
                            return;
                        }
                    }
                } catch (NumberFormatException ex) {
                    // ignore
                }
            }

            lblError.setText("Username atau password salah!");
        });

        txtPass.setOnAction(e -> btnLogin.fire());

        formBox.getChildren().addAll(lblForm, txtUser, txtPass, btnLogin, lblError);
        root.getChildren().addAll(lblJudul, lblSub, formBox);

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
                    case "Dashboard": konten.getChildren().add(buatDashboard()); break;
                    case "Pasien": konten.getChildren().add(buatPanelPasien()); break;
                    case "Dokter": konten.getChildren().add(buatPanelDokter()); break;
                    case "Antrean": konten.getChildren().add(buatPanelAntrean()); break;
                    case "Obat": konten.getChildren().add(buatPanelObat()); break;
                    case "Jadwal": konten.getChildren().add(buatPanelJadwal()); break;
                    case "Pembayaran": konten.getChildren().add(buatPanelPembayaran()); break;
                    case "Rekam Medis": konten.getChildren().add(buatPanelRekamMedis(false)); break;
                    case "Resep Obat": konten.getChildren().add(buatPanelResep(false)); break;
                    case "Nomor Antrean": konten.getChildren().add(buatPanelNomorAntrean()); break;
                    case "Rekam Medis Pribadi": konten.getChildren().add(buatPanelRekamMedisPribadi()); break;
                    case "Resep Saya": konten.getChildren().add(buatPanelResepPasien()); break;
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

        if (role.equals("admin") || role.equals("dokter")) konten.getChildren().add(buatDashboard());
        else konten.getChildren().add(buatPanelNomorAntrean());

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

        if (userLogin != null && userLogin.getRole().equals("dokter")) {
            String idDok = (dokterLogin != null) ? dokterLogin.getIdDokter() : "D001";
            String namaDok = (dokterLogin != null) ? dokterLogin.getNama() : "Dokter";

            judul.setText("Dashboard " + namaDok);
            kartu.add(buatKartu("Antrean Aktif", antrean.getSedangDitangani(idDok), "#e74c3c"), 0, 0);
            kartu.add(buatKartu("Sisa Antrean", String.valueOf(antrean.getJumlahAntrean(idDok)) + " Orang", "#e67e22"), 1, 0);

            int totalRM = 0;
            int totalResep = 0;
            if (dokterLogin != null) {
                for (RekamMedis rm : daftarRekamMedis) {
                    if (rm.getDoctor().getIdDokter().equals(dokterLogin.getIdDokter())) totalRM++;
                }
                for (Resep r : daftarResep) {
                    if (r.getRekamMedis() != null && r.getRekamMedis().getDoctor().getIdDokter().equals(dokterLogin.getIdDokter())) totalResep++;
                }
            }
            kartu.add(buatKartu("Rekam Medis Anda", String.valueOf(totalRM), "#9b59b6"), 2, 0);
            kartu.add(buatKartu("Resep Dibuat", String.valueOf(totalResep), "#2ecc71"), 3, 0);
        } else {
            kartu.add(buatKartu("Pasien", String.valueOf(daftarPasien.size()), "#3498db"), 0, 0);
            kartu.add(buatKartu("Dokter", String.valueOf(daftarDokter.size()), "#2ecc71"), 1, 0);
            kartu.add(buatKartu("Total Antrean", String.valueOf(antrean.getTotalSemuaAntrean()), "#e67e22"), 2, 0);
            kartu.add(buatKartu("Obat", String.valueOf(daftarObat.size()), "#9b59b6"), 3, 0);
            kartu.add(buatKartu("Rekam Medis", String.valueOf(daftarRekamMedis.size()), "#e74c3c"), 0, 1);
            kartu.add(buatKartu("Jadwal", String.valueOf(daftarJadwal.size()), "#1abc9c"), 1, 1);
            kartu.add(buatKartu("Resep", String.valueOf(daftarResep.size()), "#2980b9"), 2, 1);
            kartu.add(buatKartu("Pembayaran", String.valueOf(daftarPembayaran.size()), "#f39c12"), 3, 1);
        }

        panel.getChildren().addAll(judul, kartu);
        return panel;
    }

    private VBox buatKartu(String judul, String nilai, String warna) {
        VBox kartu = new VBox(8);
        kartu.setAlignment(Pos.CENTER);
        kartu.setPrefSize(160, 100);
        kartu.setStyle("-fx-background-color: " + warna + "; -fx-background-radius: 8;");

        Label lblNilai = new Label(nilai);
        lblNilai.setFont(Font.font("Arial", FontWeight.BOLD, 22));
        lblNilai.setTextFill(Color.WHITE);

        Label lblJudul = new Label(judul);
        lblJudul.setFont(Font.font("Arial", 13));
        lblJudul.setTextFill(Color.web("#ecf0f1"));

        kartu.getChildren().addAll(lblNilai, lblJudul);
        return kartu;
    }

    // ===================== PANEL PASIEN =====================
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
        ObservableList<Pasien> data = FXCollections.observableArrayList(daftarPasien);
        tabel.setItems(data);

        HBox tombol = new HBox(10);
        Button btnTambah = new Button("+ Tambah Pasien");
        Button btnHapus = new Button("Hapus");
        btnTambah.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 15; -fx-background-radius: 5;");
        btnHapus.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 15; -fx-background-radius: 5;");

        btnTambah.setOnAction(e -> {
            if (daftarDokter.isEmpty()) {
                showAlert("Peringatan", "Belum ada data dokter! Daftarkan dokter terlebih dahulu.");
                return;
            }

            Dialog<Pasien> dialog = new Dialog<>();
            dialog.setTitle("Tambah Pasien & Antrean");
            dialog.setHeaderText(null);
            ButtonType btnSimpan = new ButtonType("Simpan", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(btnSimpan, ButtonType.CANCEL);

            GridPane grid = new GridPane();
            grid.setHgap(12); grid.setVgap(12);
            grid.setPadding(new Insets(20));

            TextField txtNama = new TextField(); txtNama.setPromptText("Nama lengkap");
            TextField txtAlamat = new TextField(); txtAlamat.setPromptText("Alamat");
            TextField txtTlp = new TextField(); txtTlp.setPromptText("08xx");
            TextField txtTgl = new TextField(); txtTgl.setPromptText("dd-mm-yyyy");

            ComboBox<String> cmbJK = new ComboBox<>();
            cmbJK.getItems().addAll("Laki-laki", "Perempuan");
            cmbJK.setValue("Laki-laki");
            cmbJK.setMaxWidth(Double.MAX_VALUE);

            ComboBox<Dokter> cmbDokterTujuan = new ComboBox<>();
            cmbDokterTujuan.getItems().addAll(daftarDokter);
            if (!daftarDokter.isEmpty()) cmbDokterTujuan.setValue(daftarDokter.get(0));
            cmbDokterTujuan.setMaxWidth(Double.MAX_VALUE);

            grid.add(new Label("Nama:"), 0, 0); grid.add(txtNama, 1, 0);
            grid.add(new Label("Alamat:"), 0, 1); grid.add(txtAlamat, 1, 1);
            grid.add(new Label("No Telp:"), 0, 2); grid.add(txtTlp, 1, 2);
            grid.add(new Label("Tgl Lahir:"), 0, 3); grid.add(txtTgl, 1, 3);
            grid.add(new Label("Jenis Kelamin:"), 0, 4); grid.add(cmbJK, 1, 4);
            grid.add(new Label("Dokter Tujuan:"), 0, 5); grid.add(cmbDokterTujuan, 1, 5);

            GridPane.setHgrow(txtNama, Priority.ALWAYS);
            GridPane.setHgrow(txtAlamat, Priority.ALWAYS);
            GridPane.setHgrow(txtTlp, Priority.ALWAYS);
            GridPane.setHgrow(txtTgl, Priority.ALWAYS);

            dialog.getDialogPane().setContent(grid);
            dialog.getDialogPane().setPrefWidth(420);

            dialog.setResultConverter(btn -> {
                if (btn == btnSimpan && !txtNama.getText().trim().isEmpty()) {
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
                data.add(p);
                users.add(new User(p.getIdPasien(), p.getIdPasien(), "pasien"));

                Dokter drTerpilih = cmbDokterTujuan.getValue();
                String nomorKode = antrean.tambahAntrean(drTerpilih.getIdDokter(), p);

                showAlert("Info Pasien Baru",
                    "Pasien Berhasil Terdaftar!\n" +
                    "Username/Password: " + p.getIdPasien() + "\n\n" +
                    "--- PASIEN TERDAFTAR DAN MENDAPATKAN ANTREAN ---\n" +
                    "Dokter Tujuan: " + drTerpilih.getNama() + "\n" +
                    "Nomor Urut Antrean: " + nomorKode
                );
            });
        });

        btnHapus.setOnAction(e -> {
            Pasien selected = tabel.getSelectionModel().getSelectedItem();
            if (selected != null) {
                daftarPasien.remove(selected);
                data.remove(selected);
            } else {
                showAlert("Peringatan", "Pilih data pasien terlebih dahulu!");
            }
        });

        tombol.getChildren().addAll(btnTambah, btnHapus);
        panel.getChildren().addAll(judul, tombol, tabel);
        return panel;
    }

    // ===================== PANEL DOKTER =====================
    private VBox buatPanelDokter() {
        VBox panel = new VBox(15);
        panel.setPadding(new Insets(25));

        Label judul = new Label("Manajemen Dokter");
        judul.setFont(Font.font("Arial", FontWeight.BOLD, 22));

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

        tabel.getColumns().addAll(colId, colNama, colSp, colTlp);
        ObservableList<Dokter> data = FXCollections.observableArrayList(daftarDokter);
        tabel.setItems(data);

        HBox tombol = new HBox(10);
        Button btnTambah = new Button("+ Tambah Dokter");
        Button btnHapus = new Button("Hapus");
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
                DatabaseHelper.simpanDokter(d);
                String usernameBaru = "dokter" + Integer.parseInt(d.getIdDokter().replaceAll("\\D", ""));
                String passwordBaru = "dok123";
                DatabaseHelper.simpanUser(new User(usernameBaru, passwordBaru, "dokter"));

                daftarDokter.add(d);
                data.add(d);
                users.add(new User(usernameBaru, passwordBaru, "dokter"));

                showAlert("Sukses Tambah Dokter",
                    "Dokter " + d.getNama() + " Berhasil Didaftarkan!\n\n" +
                    "--- AKUN AKSES LOGIN DOKTER ---\n" +
                    "Username : " + usernameBaru + "\n" +
                    "Password : " + passwordBaru
                );
            });
        });

        btnHapus.setOnAction(e -> {
            Dokter selected = tabel.getSelectionModel().getSelectedItem();
            if (selected != null) {
                daftarDokter.remove(selected);
                data.remove(selected);
            } else {
                showAlert("Peringatan", "Pilih data dokter terlebih dahulu!");
            }
        });

        tombol.getChildren().addAll(btnTambah, btnHapus);
        panel.getChildren().addAll(judul, tombol, tabel);
        return panel;
    }

    // ===================== PANEL ANTREAN =====================
    private VBox buatPanelAntrean() {
        VBox panel = new VBox(15);
        panel.setPadding(new Insets(25));

        Label judul = new Label("Manajemen Antrean per Dokter");
        judul.setFont(Font.font("Arial", FontWeight.BOLD, 22));

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
            new SimpleStringProperty(d.getValue()[0]));
        TableColumn<String[], String> colId = new TableColumn<>("ID Pasien");
        colId.setCellValueFactory((TableColumn.CellDataFeatures<String[], String> d) ->
            new SimpleStringProperty(d.getValue()[1]));
        TableColumn<String[], String> colNama = new TableColumn<>("Nama Pasien");
        colNama.setCellValueFactory((TableColumn.CellDataFeatures<String[], String> d) ->
            new SimpleStringProperty(d.getValue()[2]));

        tabel.getColumns().addAll(colNo, colId, colNama);
        ObservableList<String[]> dataTabel = FXCollections.observableArrayList();
        tabel.setItems(dataTabel);

        Runnable refreshAntreanTabel = () -> {
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

        filterBox.getChildren().addAll(lblPilihDokter, cmbFilterDokter, lblJumlah);

        HBox tombol = new HBox(10);
        Button btnPanggil = new Button("Panggil Berikutnya");
        btnPanggil.setStyle("-fx-background-color: #e67e22; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 15; -fx-background-radius: 5;");

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

    // ===================== PANEL OBAT =====================
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

            dialog.showAndWait().ifPresent(o -> {
                DatabaseHelper.simpanObat(o);
                daftarObat.add(o);
                data.add(o);
            });
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

        tombol.getChildren().addAll(btnTambah, btnHapus);
        panel.getChildren().addAll(judul, tombol, tabel);
        return panel;
    }

    // ===================== PANEL JADWAL =====================
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
        colDokter.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDoctor().getNama()));
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

        tombol.getChildren().addAll(btnTambah, btnHapus);
        panel.getChildren().addAll(judul, tombol, tabel);
        return panel;
    }

    // ===================== PANEL PEMBAYARAN =====================
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
        TableColumn<Pembayaran, String> colPasien = new TableColumn<>("Nama Pasien");
        colPasien.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getRekamMedis().getPasien().getNama()));
        TableColumn<Pembayaran, String> colTgl = new TableColumn<>("Tanggal");
        colTgl.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getTanggal()));
        TableColumn<Pembayaran, Double> colTotal = new TableColumn<>("Total (Rp)");
        colTotal.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getTotalBayar()));
        TableColumn<Pembayaran, String> colStatus = new TableColumn<>("Status");
        colStatus.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getStatus()));

        tabel.getColumns().addAll(colId, colPasien, colTgl, colTotal, colStatus);
        ObservableList<Pembayaran> data = FXCollections.observableArrayList(daftarPembayaran);
        tabel.setItems(data);

        HBox tombol = new HBox(10);
        Button btnProses = new Button("Proses Pembayaran Baru");
        Button btnLunas = new Button("Tandai Lunas");
        btnProses.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 15; -fx-background-radius: 5;");
        btnLunas.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 15; -fx-background-radius: 5;");

        btnProses.setOnAction(e -> {
            ArrayList<RekamMedis> rmBelumBayar = new ArrayList<>();
            for (RekamMedis rm : daftarRekamMedis) {
                boolean sudahBayar = false;
                for (Pembayaran p : daftarPembayaran) {
                    if (p.getRekamMedis().getIdRekamMedis().equals(rm.getIdRekamMedis())) {
                        sudahBayar = true;
                        break;
                    }
                }
                if (!sudahBayar) rmBelumBayar.add(rm);
            }

            if (rmBelumBayar.isEmpty()) {
                showAlert("Info Pembayaran", "Semua pasien yang selesai diperiksa telah diproses pembayarannya / Belum ada data pemeriksaan baru!");
                return;
            }

            Dialog<Pembayaran> dialog = new Dialog<>();
            dialog.setTitle("Proses Pembayaran Baru");
            ButtonType btnSimpan = new ButtonType("Proses", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(btnSimpan, ButtonType.CANCEL);

            GridPane grid = new GridPane();
            grid.setHgap(10); grid.setVgap(10);
            grid.setPadding(new Insets(20));

            ComboBox<RekamMedis> cmbRM = new ComboBox<>();
            cmbRM.getItems().addAll(rmBelumBayar);
            cmbRM.setValue(rmBelumBayar.get(0));

            TextField txtBiaya = new TextField("50000");
            Label lblObat = new Label("Biaya Obat: Rp 0");
            Label lblTotal = new Label("Total Pembayaran: Rp 50000");
            lblTotal.setStyle("-fx-font-weight: bold; -fx-text-fill: #2196F3;");

            // Update preview saat user ubah konsultasi atau pilih pasien berbeda
            Runnable updatePreview = () -> {
                try {
                    double konsultasi = Double.parseDouble(txtBiaya.getText().trim());
                    double biayaObat = cmbRM.getValue() != null ? cmbRM.getValue().getTotalBiayaObat() : 0;
                    double total = konsultasi + biayaObat;
                    lblObat.setText(String.format("Biaya Obat: Rp %.0f", biayaObat));
                    lblTotal.setText(String.format("Total Pembayaran: Rp %.0f", total));
                } catch (NumberFormatException ex) {
                    lblTotal.setText("Total Pembayaran: Rp 0");
                }
            };

            txtBiaya.textProperty().addListener((obs, oldVal, newVal) -> updatePreview.run());
            cmbRM.valueProperty().addListener((obs, oldVal, newVal) -> updatePreview.run());
            updatePreview.run();

            grid.add(new Label("Pasien (Selesai Periksa):"), 0, 0); grid.add(cmbRM, 1, 0);
            grid.add(new Label("Biaya Konsultasi (Rp):"), 0, 1); grid.add(txtBiaya, 1, 1);
            grid.add(lblObat, 1, 2);
            grid.add(lblTotal, 1, 3);
            dialog.getDialogPane().setContent(grid);

                dialog.setResultConverter(btn -> {
                    if (btn == btnSimpan && cmbRM.getValue() != null) {
                        String id = String.format("PAY%03d", idBayarCounter++);
                        double biayaKonsultasi;
                        try {
                            biayaKonsultasi = Double.parseDouble(txtBiaya.getText().trim());
                        } catch (NumberFormatException ex) {
                            biayaKonsultasi = 0;
                        }
                        return new Pembayaran(id, cmbRM.getValue(), biayaKonsultasi,
                            java.time.LocalDate.now().toString());
                }
                return null;
            });

            dialog.showAndWait().ifPresent(p -> {
                daftarPembayaran.add(p);
                data.add(p);
            });
        });

        btnLunas.setOnAction(e -> {
            Pembayaran selected = tabel.getSelectionModel().getSelectedItem();
            if (selected != null) {
                selected.bayar();
                tabel.refresh();
            } else {
                showAlert("Peringatan", "Pilih transaksi di tabel terlebih dahulu!");
            }
        });

        tombol.getChildren().addAll(btnProses, btnLunas);
        panel.getChildren().addAll(judul, tombol, tabel);
        return panel;
    }

    // ===================== PANEL REKAM MEDIS =====================
    private VBox buatPanelRekamMedis(boolean readOnly) {
        VBox panel = new VBox(15);
        panel.setPadding(new Insets(25));

        Label judul = new Label("Rekam Medis");
        judul.setFont(Font.font("Arial", FontWeight.BOLD, 22));

        TableView<RekamMedis> tabel = new TableView<>();
        tabel.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(tabel, Priority.ALWAYS);

        TableColumn<RekamMedis, String> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getIdRekamMedis()));
        TableColumn<RekamMedis, String> colPasien = new TableColumn<>("Pasien");
        colPasien.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getPasien().getNama()));
        TableColumn<RekamMedis, String> colTgl = new TableColumn<>("Tanggal");
        colTgl.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getTanggal()));
        TableColumn<RekamMedis, String> colKeluhan = new TableColumn<>("Keluhan");
        colKeluhan.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getKeluhan()));
        TableColumn<RekamMedis, String> colDiagnosis = new TableColumn<>("Diagnosis");
        colDiagnosis.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDiagnosis()));

        tabel.getColumns().addAll(colId, colPasien, colTgl, colKeluhan, colDiagnosis);

        ObservableList<RekamMedis> data = FXCollections.observableArrayList();
        if (userLogin.getRole().equals("dokter") && dokterLogin != null) {
            for (RekamMedis rm : daftarRekamMedis) {
                if (rm.getDoctor().getIdDokter().equals(dokterLogin.getIdDokter())) {
                    data.add(rm);
                }
            }
        } else {
            data.addAll(daftarRekamMedis);
        }
        tabel.setItems(data);

        panel.getChildren().addAll(judul, tabel);

        if (!readOnly && userLogin.getRole().equals("dokter")) {
            HBox tombol = new HBox(10);
            Button btnTambah = new Button("+ Input Rekam Medis");
            btnTambah.setStyle("-fx-background-color: #9b59b6; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 15; -fx-background-radius: 5;");

            btnTambah.setOnAction(e -> {
                ArrayList<Pasien> pasienAktif = new ArrayList<>();
                if (dokterLogin != null) {
                    Pasien pSedangDitangani = antrean.getPasienSedangDitangani(dokterLogin.getIdDokter());
                    if (pSedangDitangani != null) {
                        pasienAktif.add(pSedangDitangani);
                    }
                }

                if (pasienAktif.isEmpty()) {
                    showAlert("Peringatan", "Belum ada pasien yang dipanggil / sedang ditangani oleh Anda saat ini!\nPanggil pasien dari menu Antrean terlebih dahulu.");
                    return;
                }

                Dialog<RekamMedis> dialog = new Dialog<>();
                dialog.setTitle("Input Rekam Medis");
                ButtonType btnSimpan = new ButtonType("Simpan", ButtonBar.ButtonData.OK_DONE);
                dialog.getDialogPane().getButtonTypes().addAll(btnSimpan, ButtonType.CANCEL);

                GridPane grid = new GridPane();
                grid.setHgap(10); grid.setVgap(10);
                grid.setPadding(new Insets(20));

                ComboBox<Pasien> cmbPasien = new ComboBox<>();
                cmbPasien.getItems().addAll(pasienAktif);
                cmbPasien.setValue(pasienAktif.get(0));

                TextField txtKeluhan = new TextField();
                TextField txtDiagnosis = new TextField();

                grid.add(new Label("Pasien:"), 0, 0); grid.add(cmbPasien, 1, 0);
                grid.add(new Label("Keluhan:"), 0, 1); grid.add(txtKeluhan, 1, 1);
                grid.add(new Label("Diagnosis:"), 0, 2); grid.add(txtDiagnosis, 1, 2);
                dialog.getDialogPane().setContent(grid);

                dialog.setResultConverter(b -> b == btnSimpan ?
                    new RekamMedis(String.format("RM%03d", idRMCounter++), cmbPasien.getValue(),
                        dokterLogin, java.time.LocalDate.now().toString(),
                        txtKeluhan.getText(), txtDiagnosis.getText()) : null);

                dialog.showAndWait().ifPresent(rm -> {
                    daftarRekamMedis.add(rm);
                    data.add(rm);
                });
            });

            tombol.getChildren().add(btnTambah);
            panel.getChildren().add(1, tombol);
        }
        return panel;
    }

    // ===================== PANEL RESEP OBAT =====================
    private VBox buatPanelResep(boolean readOnly) {
        VBox panel = new VBox(15);
        panel.setPadding(new Insets(25));

        Label judul = new Label("Resep Obat");
        judul.setFont(Font.font("Arial", FontWeight.BOLD, 22));

        TableView<Resep> tabel = new TableView<>();
        tabel.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(tabel, Priority.ALWAYS);

        TableColumn<Resep, String> colId = new TableColumn<>("ID Resep");
        colId.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getIdResep()));
        TableColumn<Resep, String> colPasien = new TableColumn<>("Nama Pasien");
        colPasien.setCellValueFactory(cellData -> new SimpleStringProperty(
            cellData.getValue().getRekamMedis() != null ? cellData.getValue().getRekamMedis().getPasien().getNama() : "-"));
        TableColumn<Resep, String> colObat = new TableColumn<>("Nama Obat");
        colObat.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getObat().getNamaObat()));
        TableColumn<Resep, Integer> colJumlah = new TableColumn<>("Jumlah");
        colJumlah.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getJumlah()));
        TableColumn<Resep, String> colAturan = new TableColumn<>("Aturan Pakai");
        colAturan.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getAturanPakai()));

        tabel.getColumns().addAll(colId, colPasien, colObat, colJumlah, colAturan);

        ObservableList<Resep> data = FXCollections.observableArrayList();
        if (userLogin.getRole().equals("dokter") && dokterLogin != null) {
            for (Resep r : daftarResep) {
                if (r.getRekamMedis() != null && r.getRekamMedis().getDoctor().getIdDokter().equals(dokterLogin.getIdDokter())) {
                    data.add(r);
                }
            }
        } else {
            data.addAll(daftarResep);
        }
        tabel.setItems(data);

        panel.getChildren().add(judul);

        if (!readOnly && userLogin.getRole().equals("dokter")) {
            HBox tombol = new HBox(10);
            Button btnTambah = new Button("+ Tambah Resep");
            btnTambah.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 15; -fx-background-radius: 5;");

            btnTambah.setOnAction(e -> {
                Pasien pasienSedang = null;
                if (dokterLogin != null) {
                    pasienSedang = antrean.getPasienSedangDitangani(dokterLogin.getIdDokter());
                }

                if (pasienSedang == null) {
                    showAlert("Peringatan", "Belum ada pasien yang sedang diperiksa oleh Anda saat ini.\nPanggil pasien dari menu Antrean terlebih dahulu.");
                    return;
                }

                ArrayList<RekamMedis> rmDokter = new ArrayList<>();
                if (dokterLogin != null) {
                    for (RekamMedis rm : daftarRekamMedis) {
                        if (rm.getDoctor().getIdDokter().equals(dokterLogin.getIdDokter()) &&
                            rm.getPasien().getIdPasien().equals(pasienSedang.getIdPasien())) {
                            boolean sudahBayar = false;
                            for (Pembayaran p : daftarPembayaran) {
                                if (p.getRekamMedis().getIdRekamMedis().equals(rm.getIdRekamMedis())) {
                                    sudahBayar = true;
                                    break;
                                }
                            }
                            if (!sudahBayar) rmDokter.add(rm);
                        }
                    }
                }

                if (rmDokter.isEmpty()) {
                    showAlert("Peringatan", "Belum ada rekam medis aktif untuk pasien yang sedang diperiksa atau pasien tersebut sudah dibayar.");
                    return;
                }
                if (daftarObat.isEmpty()) {
                    showAlert("Error", "Belum ada data obat!");
                    return;
                }

                Dialog<Resep> dialog = new Dialog<>();
                dialog.setTitle("Tambah Resep Obat");
                dialog.setHeaderText(null);
                ButtonType btnSimpan = new ButtonType("Simpan", ButtonBar.ButtonData.OK_DONE);
                dialog.getDialogPane().getButtonTypes().addAll(btnSimpan, ButtonType.CANCEL);

                GridPane grid = new GridPane();
                grid.setHgap(12); grid.setVgap(12);
                grid.setPadding(new Insets(20));

                ComboBox<RekamMedis> cmbRM = new ComboBox<>();
                cmbRM.getItems().addAll(rmDokter);
                cmbRM.setValue(rmDokter.get(0));
                cmbRM.setMaxWidth(Double.MAX_VALUE);

                ComboBox<Obat> cmbObat = new ComboBox<>();
                cmbObat.getItems().addAll(daftarObat);
                cmbObat.setValue(daftarObat.get(0));
                cmbObat.setMaxWidth(Double.MAX_VALUE);

                TextField txtJumlah = new TextField("1");
                TextField txtAturan = new TextField();
                txtAturan.setPromptText("3x sehari setelah makan");

                grid.add(new Label("Rekam Medis:"), 0, 0); grid.add(cmbRM, 1, 0);
                grid.add(new Label("Obat:"), 0, 1); grid.add(cmbObat, 1, 1);
                grid.add(new Label("Jumlah:"), 0, 2); grid.add(txtJumlah, 1, 2);
                grid.add(new Label("Aturan Pakai:"), 0, 3); grid.add(txtAturan, 1, 3);

                GridPane.setHgrow(cmbRM, Priority.ALWAYS);
                GridPane.setHgrow(cmbObat, Priority.ALWAYS);

                dialog.getDialogPane().setContent(grid);
                dialog.getDialogPane().setPrefWidth(450);

                dialog.setResultConverter(btn -> {
                    if (btn == btnSimpan && cmbObat.getValue() != null && cmbRM.getValue() != null) {
                        int jumlahObat;
                        try {
                            jumlahObat = Integer.parseInt(txtJumlah.getText().trim());
                        } catch (NumberFormatException ex) {
                            showAlert("Error", "Jumlah obat harus berupa angka.");
                            return null;
                        }
                        if (jumlahObat <= 0) {
                            showAlert("Error", "Jumlah obat harus lebih dari 0.");
                            return null;
                        }
                        if (jumlahObat > cmbObat.getValue().getStok()) {
                            showAlert("Error", "Stok obat tidak cukup. Stok tersedia: " + cmbObat.getValue().getStok());
                            return null;
                        }
                        String id = String.format("RSP%03d", idResepCounter++);
                        return new Resep(id, cmbRM.getValue(), cmbObat.getValue(), jumlahObat, txtAturan.getText().trim());
                    }
                    return null;
                });

                dialog.showAndWait().ifPresent(r -> {
                    daftarResep.add(r);
                    r.getRekamMedis().tambahResep(r);
                    r.getObat().kurangiStok(r.getJumlah());
                    DatabaseHelper.updateObatStok(r.getObat());
                    data.add(r);
                    tabel.refresh();
                    showAlert("Sukses", "Resep " + r.getIdResep() + " berhasil ditambahkan!\n" +
                        "Pasien: " + r.getRekamMedis().getPasien().getNama() + "\n" +
                        "Obat: " + r.getObat().getNamaObat() + " x" + r.getJumlah() + "\n" +
                        "Aturan: " + r.getAturanPakai());
                });
            });

            tombol.getChildren().add(btnTambah);
            panel.getChildren().add(tombol);
        }

        panel.getChildren().add(tabel);
        return panel;
    }

    // ===================== PANEL NOMOR ANTREAN (PASIEN) =====================
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

        Label lblPesanKosong = new Label("Anda belum terdaftar di antrean dokter manapun saat ini.");
        lblPesanKosong.setFont(Font.font("Arial", 14));

        Runnable refreshStatus = () -> {
            infoAntrean.getChildren().clear();
            if (pasienLogin == null) {
                infoAntrean.add(lblPesanKosong, 0, 0);
                return;
            }

            Dokter drTujuan = null;
            int sisaDidepan = -1;

            for (Dokter d : daftarDokter) {
                int sisa = antrean.getSisaDidepan(d.getIdDokter(), pasienLogin);
                if (sisa != -1) {
                    drTujuan = d;
                    sisaDidepan = sisa;
                    break;
                }
            }

            if (drTujuan != null) {
                Label valDr = new Label(drTujuan.getNama());
                valDr.setStyle("-fx-font-weight: bold;");
                Label valNoSaya = new Label(antrean.getKodeAntrean(pasienLogin));
                valNoSaya.setStyle("-fx-font-weight: bold; -fx-text-fill: #2ecc71; -fx-font-size: 18;");
                Label valSisa = new Label(sisaDidepan == 0 ? "Giliran Anda Berikutnya!" : sisaDidepan + " Orang Lagi");
                valSisa.setStyle("-fx-font-weight: bold; -fx-text-fill: #e67e22; -fx-font-size: 14;");
                Label valSedang = new Label(antrean.getSedangDitangani(drTujuan.getIdDokter()));
                valSedang.setStyle("-fx-font-weight: bold; -fx-text-fill: #e74c3c; -fx-font-size: 14;");

                infoAntrean.add(new Label("Dokter Tujuan:"), 0, 0); infoAntrean.add(valDr, 1, 0);
                infoAntrean.add(new Label("Nomor Antrean Anda:"), 0, 1); infoAntrean.add(valNoSaya, 1, 1);
                infoAntrean.add(new Label("Sisa Antrean di Depan:"), 0, 2); infoAntrean.add(valSisa, 1, 2);
                infoAntrean.add(new Label("Antrean Sedang Ditangani:"), 0, 3); infoAntrean.add(valSedang, 1, 3);
            } else {
                infoAntrean.add(lblPesanKosong, 0, 0);
            }
        };

        refreshStatus.run();

        Button btnRefresh = new Button("Refresh Status Antrean");
        btnRefresh.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 15; -fx-background-radius: 5;");
        btnRefresh.setOnAction(e -> refreshStatus.run());

        panel.getChildren().addAll(judul, lblNama, infoAntrean, btnRefresh);
        return panel;
    }

    // ===================== PANEL REKAM MEDIS PRIBADI (PASIEN) =====================
    private VBox buatPanelRekamMedisPribadi() {
        VBox panel = new VBox(15);
        panel.setPadding(new Insets(25));

        Label judul = new Label("Rekam Medis Pribadi");
        judul.setFont(Font.font("Arial", FontWeight.BOLD, 22));

        TableView<RekamMedis> tabel = new TableView<>();
        tabel.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(tabel, Priority.ALWAYS);

        TableColumn<RekamMedis, String> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getIdRekamMedis()));
        TableColumn<RekamMedis, String> colTgl = new TableColumn<>("Tanggal");
        colTgl.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getTanggal()));
        TableColumn<RekamMedis, String> colDokter = new TableColumn<>("Dokter");
        colDokter.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDoctor().getNama()));
        TableColumn<RekamMedis, String> colKeluhan = new TableColumn<>("Keluhan");
        colKeluhan.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getKeluhan()));
        TableColumn<RekamMedis, String> colDiagnosis = new TableColumn<>("Diagnosis");
        colDiagnosis.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDiagnosis()));

        tabel.getColumns().addAll(colId, colTgl, colDokter, colKeluhan, colDiagnosis);

        ObservableList<RekamMedis> data = FXCollections.observableArrayList();
        if (pasienLogin != null) {
            for (RekamMedis rm : daftarRekamMedis) {
                if (rm.getPasien().getIdPasien().equals(pasienLogin.getIdPasien())) {
                    data.add(rm);
                }
            }
        }
        tabel.setItems(data);

        panel.getChildren().addAll(judul, tabel);
        return panel;
    }

    // ===================== PANEL RESEP PASIEN =====================
    private VBox buatPanelResepPasien() {
        VBox panel = new VBox(15);
        panel.setPadding(new Insets(25));

        Label judul = new Label("Resep Obat Saya");
        judul.setFont(Font.font("Arial", FontWeight.BOLD, 22));

        TableView<Resep> tabel = new TableView<>();
        tabel.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(tabel, Priority.ALWAYS);

        TableColumn<Resep, String> colId = new TableColumn<>("ID Resep");
        colId.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getIdResep()));
        TableColumn<Resep, String> colObat = new TableColumn<>("Nama Obat");
        colObat.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getObat().getNamaObat()));
        TableColumn<Resep, Integer> colJumlah = new TableColumn<>("Jumlah");
        colJumlah.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getJumlah()));
        TableColumn<Resep, String> colAturan = new TableColumn<>("Aturan Pakai");
        colAturan.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getAturanPakai()));
        TableColumn<Resep, String> colDokter = new TableColumn<>("Dokter");
        colDokter.setCellValueFactory(cellData -> new SimpleStringProperty(
            cellData.getValue().getRekamMedis() != null ? cellData.getValue().getRekamMedis().getDoctor().getNama() : "-"));

        tabel.getColumns().addAll(colId, colObat, colJumlah, colAturan, colDokter);

        ObservableList<Resep> data = FXCollections.observableArrayList();
        if (pasienLogin != null) {
            for (Resep r : daftarResep) {
                if (r.getRekamMedis() != null && r.getRekamMedis().getPasien().getIdPasien().equals(pasienLogin.getIdPasien())) {
                    data.add(r);
                }
            }
        }
        tabel.setItems(data);

        panel.getChildren().addAll(judul, tabel);
        return panel;
    }

    // ===================== UTILITY =====================
    private void showAlert(String judul, String pesan) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(judul);
        alert.setHeaderText(null);
        alert.setContentText(pesan);
        alert.showAndWait();
    }
}