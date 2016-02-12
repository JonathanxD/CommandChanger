package github.therealbuggy.commandchanger.api;

@FunctionalInterface
public interface CommandChanger {

    void receiveCommandRegister(CCommand command);

}
