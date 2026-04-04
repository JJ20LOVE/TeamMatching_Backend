package club.boyuan.official.teammatching.mq.support;

/**
 * 用户通知开关：与库字段一致，{@code null} 视为开启（兼容旧数据与默认行为）。
 */
public final class NotificationPreferenceUtils {

    private NotificationPreferenceUtils() {
    }

    public static boolean isChannelEnabled(Boolean flag) {
        return flag == null || flag;
    }
}
