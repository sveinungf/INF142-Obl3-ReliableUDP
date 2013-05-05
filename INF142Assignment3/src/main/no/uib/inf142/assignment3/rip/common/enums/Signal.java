package no.uib.inf142.assignment3.rip.common.enums;

public enum Signal {

    REGULAR("R"),
    PARTIAL("P"),

    SYN("S"),
    ACK("A"),
    SYNACK("Y"),
    FIN("F");

    private String string;

    private Signal(final String string) {
        this.string = string;
    }

    public String getString() {
        return string;
    }
}
