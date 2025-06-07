package newcode.fun.control.cmd.impl;

import net.minecraft.client.Minecraft;
import net.minecraft.util.text.TextFormatting;
import newcode.fun.control.cmd.Cmd;
import newcode.fun.control.cmd.CmdInfo;
import newcode.fun.control.friend.Friend;
import newcode.fun.control.friend.FriendManager;
import newcode.fun.control.Manager;
import newcode.fun.utils.language.Translated;


@CmdInfo(name = "friend", description = "��������� �������� ������� � ������ ������", descriptionen = "Allows you to add a person to your friends list")
public class FriendCmd extends Cmd {

    @Override
    public void run(String[] args) throws Exception {
        if (args.length > 1) {
            switch (args[1]) {
                case "add" -> {
                    final String friendName = args[2];
                    addFriend(friendName);
                }
                case "remove" -> {
                    final String friendName = args[2];
                    removeFriend(friendName);
                }
                case "list" -> friendList();
                case "clear" -> clearFriendList();
            }
        } else {
            error();
        }
    }

    /**
     * ������� ���������� ������ � ������
     *
     * @param friendName ��� ������
     */
    private void addFriend(final String friendName) {
        final FriendManager friendManager = Manager.FRIEND_MANAGER;

        if (friendName.contains(Minecraft.getInstance().player.getName().getString())) {
            sendMessage(Translated.isRussian() ? "Unfortunately you can't add yourself as a friend :(" : "� ��������� �� �� ������ �������� ������ ���� � ������ :(");
            return;
        }
        if (friendManager.getFriends().stream().map(Friend::getName).toList().contains(friendName)) {
            sendMessage(Translated.isRussian() ? friendName + " already on your friends list" : friendName + " ��� ���� � ������ ������");
            return;
        }

        sendMessage(Translated.isRussian() ? friendName + " successfully added to your friends list!" : friendName + " ������� �������� � ������ ������!");
        friendManager.addFriend(friendName);
    }

    /**
     * ������� �������� ������ �� ������ ������
     *
     * @param friendName ��� ������
     */
    private void removeFriend(final String friendName) {
        final FriendManager friendManager = Manager.FRIEND_MANAGER;

        if (friendManager.isFriend(friendName)) {
            friendManager.removeFriend(friendName);
            sendMessage(Translated.isRussian() ? friendName + " has been removed from your friends list" : friendName + " ��� ������ �� ������ ������");
            return;
        }
        sendMessage(Translated.isRussian() ? friendName + " not on the friends list" : friendName + " ���� � ������ ������");
    }

    /**
     * ������� ������ ����� ���� ������
     */
    private void friendList() {
        final FriendManager friendManager = Manager.FRIEND_MANAGER;

        if (friendManager.getFriends().isEmpty()) {
            sendMessage(Translated.isRussian() ? "Friends list is empty" : "������ ������ ����");
            return;
        }

        sendMessage(Translated.isRussian() ? "Friends list:" : "������ ������:");
        for (Friend friend : friendManager.getFriends()) {
            sendMessage(TextFormatting.GRAY + friend.getName());
        }
    }

    /**
     * ������� ������ ������
     */
    private void clearFriendList() {
        final FriendManager friendManager = Manager.FRIEND_MANAGER;

        if (friendManager.getFriends().isEmpty()) {
            sendMessage(Translated.isRussian() ? "Friends list is empty" : "������ ������ ����");
            return;
        }

        friendManager.clearFriend();
        sendMessage(Translated.isRussian() ? "The friends list has been successfully cleared" : "������ ������ ������� ������");
    }

    @Override
    public void error() {
        sendMessage(Translated.isRussian() ? TextFormatting.GRAY + "Error in use" + TextFormatting.WHITE + ":" : TextFormatting.GRAY + "������ � �������������" + TextFormatting.WHITE + ":");
        sendMessage(TextFormatting.WHITE + "." + "friend add " + TextFormatting.GRAY + "<"
                + TextFormatting.RED + "name" + TextFormatting.GRAY + ">");
        sendMessage(TextFormatting.WHITE + "." + "friend remove " + TextFormatting.GRAY + "<"
                + TextFormatting.RED + "name" + TextFormatting.GRAY + ">");
        sendMessage(TextFormatting.WHITE + "." + "friend list" + TextFormatting.GRAY + (Translated.isRussian() ? " - shows a list of all friends" : " - ���������� ������ ���� ��������"));
        sendMessage(TextFormatting.WHITE + "." + "friend clear" + TextFormatting.GRAY + (Translated.isRussian() ? " - clears all friends" : " - ������� ���� ��������"));
    }
}
