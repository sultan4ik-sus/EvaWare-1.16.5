package newcode.fun.control.cmd.impl;

import net.minecraft.util.text.TextFormatting;
import newcode.fun.control.cmd.Cmd;
import newcode.fun.control.cmd.CmdInfo;
import newcode.fun.control.Manager;
import newcode.fun.control.staff.StaffManager;
import newcode.fun.utils.language.Translated;

import java.util.List;


@CmdInfo(name = "staff", description = "��������� ������ � Staf-List", descriptionen = "Adds a player to the Staf-List")
public class StaffCmd extends Cmd {
    @Override
    public void run(String[] args) throws Exception {
        if (args.length >= 2) {
            switch (args[1].toLowerCase()) {
                case "add" -> addStaffName(args[2]);
                case "remove" -> removeStaffName(args[2]);
                case "clear" -> clearList();
                case "list" -> outputList();
            }
        } else {
            error();
        }
    }

    private void addStaffName(String name) {
        StaffManager manager = Manager.STAFF_MANAGER;

        if (manager.getStaffNames().contains(name)) {
            sendMessage(Translated.isRussian() ? TextFormatting.RED + "This player is already on the Staff List!" : TextFormatting.RED + "���� ����� ��� � Staff List!");
        } else {
            manager.addStaff(name);
            sendMessage(Translated.isRussian() ? TextFormatting.GREEN + "Nick " + TextFormatting.WHITE + name + TextFormatting.GREEN + " added to Staff List" : TextFormatting.GREEN + "��� " + TextFormatting.WHITE + name + TextFormatting.GREEN + " �������� � Staff List");
        }
    }

    private void removeStaffName(String name) {
        StaffManager manager = Manager.STAFF_MANAGER;

        if (manager.getStaffNames().contains(name)) {
            manager.removeStaff(name);
            sendMessage(Translated.isRussian() ? TextFormatting.GREEN + "Nick " + TextFormatting.WHITE + name + TextFormatting.GREEN + " removed from Staff List" : TextFormatting.GREEN + "��� " + TextFormatting.WHITE + name + TextFormatting.GREEN + " ������ �� Staff List");
        } else {
            sendMessage(Translated.isRussian() ? TextFormatting.RED + "This player is not on the Staff List!" : TextFormatting.RED + "����� ������ ��� � Staff List!");
        }
    }

    private void clearList() {
        StaffManager manager = Manager.STAFF_MANAGER;

        if (manager.getStaffNames().isEmpty()) {
            sendMessage(Translated.isRussian() ? TextFormatting.RED + "Staff List is empty!" : TextFormatting.RED + "Staff List ����!");
        } else {
            manager.clearStaffs();
            sendMessage(Translated.isRussian() ? TextFormatting.GREEN + "Staff List cleared" : TextFormatting.GREEN + "Staff List ������");
        }
    }

    private void outputList() {
        StaffManager manager = Manager.STAFF_MANAGER;

        List<String> staffNames = manager.getStaffNames();

        if (staffNames.size() > 10) {
            sendMessage(Translated.isRussian()
                    ? TextFormatting.RED + "Staff list is too large, check the config folder!"
                    : TextFormatting.RED + "������� ������� ����� ����, ���������� � ����� � ��������!");
        } else {
            sendMessage(Translated.isRussian() ? TextFormatting.GRAY + "Staff List:" : TextFormatting.GRAY + "������ Staff:");

            for (String name : staffNames) {
                sendMessage(TextFormatting.WHITE + name);
            }
        }
    }


    @Override
    public void error() {
        sendMessage(TextFormatting.GRAY + "������ � �������������" + TextFormatting.WHITE + ":");
        sendMessage(TextFormatting.WHITE + ".staff " + TextFormatting.GRAY + "<"
                + TextFormatting.RED + "add; remove; clear; list." + TextFormatting.GRAY + ">");
    }
}
