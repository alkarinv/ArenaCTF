package mc.alk.ctf;

import mc.alk.arena.controllers.StateController;
import mc.alk.arena.objects.CompetitionTransition;

/**
 * @author alkarin
 */
public enum CTFTransition implements CompetitionTransition {
    ONFLAGCAPTURE("onFlagCapture"), ONFLAGPICKUP("onFlagPickup"), ONFLAGDROP("onFlagDrop"),
    ONFLAGRETURN("onFlagReturn");

    String name;
    int globalOrdinal;

    CTFTransition(String name) {
        globalOrdinal = StateController.register(this);
        this.name = name;
    }

    public static CTFTransition fromString(String str){
        str = str.toUpperCase();
        try{
            return CTFTransition.valueOf(str);
        } catch (Exception e){
            return null;
        }
    }

    @Override
    public int globalOrdinal() {
        return globalOrdinal;
    }
    @Override
    public String toString(){
        return name;
    }
}
