public class Dokter {
    private String idDokter;
    private String nama;
    private String spesialisasi;
    private String noTelp;

    public Dokter(String idDokter, String nama, String spesialisasi, String noTelp) {
        this.idDokter = idDokter;
        this.nama = nama;
        this.spesialisasi = spesialisasi;
        this.noTelp = noTelp;
    }

    public String getIdDokter() { return idDokter; }
    public String getNama() { return nama; }
    public String getSpesialisasi() { return spesialisasi; }
    public String getNoTelp() { return noTelp; }

    public String[] toTableRow() {
        return new String[]{idDokter, nama, spesialisasi, noTelp};
    }
    @Override
public String toString() {
    return nama + " - " + spesialisasi;
}
}