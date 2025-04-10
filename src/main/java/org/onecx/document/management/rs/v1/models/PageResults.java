package org.onecx.document.management.rs.v1.models;

import java.util.List;

//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//
import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public class PageResults<T> {
    private long totalElements;
    private int number;
    private int size;
    private long totalPages;
    private List<T> stream;

    public PageResults() {
    }

    public long getTotalElements() {
        return this.totalElements;
    }

    public void setTotalElements(long totalElements) {
        this.totalElements = totalElements;
    }

    public int getNumber() {
        return this.number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public int getSize() {
        return this.size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public long getTotalPages() {
        return this.totalPages;
    }

    public void setTotalPages(long totalPages) {
        this.totalPages = totalPages;
    }

    public List<T> getStream() {
        return this.stream;
    }

    public void setStream(List<T> stream) {
        this.stream = stream;
    }
}
