package org.example.model;

public class Lab {
    private int id;
    private String roomNumber;
    private int capacity;

    public Lab() {
    }

    public Lab(int id, String roomNumber, int capacity) {
        this.id = id;
        this.roomNumber = roomNumber;
        this.capacity = capacity;
    }

    // Generate Getters and Setters...
    public int getId() {return id;}
    public void setId(int id) {this.id = id;}
    public String getRoomNumber() {return roomNumber;}
    public void setRoomNumber(String roomNumber) {this.roomNumber = roomNumber;}
    public int getCapacity() {return capacity;}
    public void setCapacity(int capacity) {this.capacity = capacity;}

}
