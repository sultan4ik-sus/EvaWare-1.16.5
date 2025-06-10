package eva.ware.command.feature;

import eva.ware.command.interfaces.Command;
import eva.ware.command.interfaces.Logger;
import eva.ware.command.interfaces.Parameters;
import eva.ware.utils.client.WHook;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.experimental.FieldDefaults;
import net.minecraft.util.text.TextFormatting;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ReportCommand implements Command {
    final Logger logger;

    @Override
    public void execute(Parameters parameters) {
        String message = parameters.collectMessage(0).trim();
        if (!message.isEmpty()) {
            sendReport(message);
        } else {
            sendError();
        }
    }

    @SneakyThrows
    private void sendReport(String message) {
        WHook.sendMessageToDiscord("report: " + message, true, false);
        logger.log("Успешно.");
    }

    private void sendError() {
        logger.log(TextFormatting.RED + "Ошибка в использовании:");
        logger.log(TextFormatting.GRAY + "Используйте .report <text>");
        logger.log(TextFormatting.GREEN + "Пример: .report HitAura не бьет слаймов");
    }

    @Override
    public String name() {
        return "report";
    }

    @Override
    public String description() {
        return "Отправляет баги в клиенте.";
    }
}
