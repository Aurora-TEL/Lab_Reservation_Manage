package org.example.model;

import java.sql.Date;
import java.time.LocalDate;

public class LabSlot {
    private int id;
    private int labId;
    private Date dateSlot;
    private String period;
    private boolean isAvailable;

    public LabSlot() {
    }

    public LabSlot(int id, int labId, Date dateSlot, String period, boolean isAvailable) {
        this.id = id;
        this.labId = labId;
        this.dateSlot = dateSlot;
        this.period = period;
        this.isAvailable = isAvailable;
    }

    // Generate Getters and Setters...
    public int getId() {return id;}
    public void setId(int id) {this.id = id;}
    public int getLabId() {return labId;}
    public void setLabId(int labId) {this.labId = labId;}
    public Date getDateSlot() {return dateSlot;}
    public void setDateSlot(Date dateSlot) {this.dateSlot = dateSlot;}
    public String getPeriod() {return period;}
    public void setPeriod(String period) {this.period = period;}
    public boolean isAvailable() {return isAvailable;}
    public void setAvailable(boolean available) {isAvailable = available;}

}
