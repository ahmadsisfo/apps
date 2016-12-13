package com.fti.sisfo.tours.volley;


public class ListData {
    private int id;
    private String name, phone, address, gambar, thumb, geom, point, kategori, city, top;

    public ListData() {
        super();
    }

    public ListData(int id, String judul, String datetime, String isi, String gambar,String thumb, String geom, String point,String top, String city) {
        super();
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.gambar = gambar;
        this.thumb = thumb;
        this.address = address;
        this.point = point;
        this.geom = geom;
        this.top = top;
        this.kategori = kategori;
        this.city = city;
    }

    public int getId() {return id;}
    public void setId(int id) {this.id = id;}

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public String getGambar() {
        return gambar;
    }
    public void setGambar(String gambar) {this.gambar = gambar;}

    public String getThumb() {
        return thumb;
    }
    public void setThumb(String thumb) {this.thumb = thumb;}

    public String getPhone() {
        return phone;
    }
    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }
    public void setAddress(String address) {
        this.address = address;
    }

    public String getGeom() {
        return geom;
    }
    public void setGeom(String geom) {
        this.geom = geom;
    }

    public String getTop() {
        return top;
    }
    public void setTop(String top) {
        this.top = top;
    }

    public String getPoint() {
        return point;
    }
    public void setPoint(String point) {
        this.point = point;
    }

    public String getKategori() {
        return kategori;
    }
    public void setKategori(String kategori) {
        this.kategori = kategori;
    }

    public String getCity() {
        return city;
    }
    public void setCity(String city) {
        this.city = city;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + id;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ListData other = (ListData) obj;
        if (id != other.id)
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "Objek [id=" + id +
                ", name="     + name +
                ", phone="    + phone +
                ", address="  + address +
                ", gambar="   + gambar +
                ", thumb="    + thumb +
                ", kategori=" + kategori +
                ", point="    + point +
                ", geom="     + geom +
                "]";
    }
}