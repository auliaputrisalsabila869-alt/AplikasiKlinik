public class Pasien {
    private String idPasien;
    private String nama;
    private String alamat;
    private String noTelp;
    private String tanggalLahir;
    private String jenisKelamin;

    public Pasien(String idPasien, String nama, String alamat,
                  String noTelp, String tanggalLahir, String jenisKelamin) {
        this.idPasien = idPasien;
        this.nama = nama;
        this.alamat = alamat;
        this.noTelp = noTelp;
        this.tanggalLahir = tanggalLahir;
        this.jenisKelamin = jenisKelamin;
    }

    public String getIdPasien() { return idPasien; }
    public String getNama() { return nama; }
    public String getAlamat() { return alamat; }
    public String getNoTelp() { return noTelp; }
    public String getTanggalLahir() { return tanggalLahir; }
    public String getJenisKelamin() { return jenisKelamin; }

    public void setNama(String nama) { this.nama = nama; }
    public void setAlamat(String alamat) { this.alamat = alamat; }
    public void setNoTelp(String noTelp) { this.noTelp = noTelp; }

    public String[] toTableRow() {
        return new String[]{idPasien, nama, jenisKelamin, tanggalLahir, noTelp, alamat};
    }
}