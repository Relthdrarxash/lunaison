package fr.lunaison.ui

import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import fr.lunaison.core.data.Partner
import fr.lunaison.core.data.PartnerChannel

actual object ShareLauncher {
    private const val WHATSAPP = "com.whatsapp"
    private const val WHATSAPP_BUSINESS = "com.whatsapp.w4b"
    private const val SIGNAL = "org.thoughtcrime.securesms"

    actual fun send(context: PlatformContext, partner: Partner, text: String) {
        val intent = when (partner.channel) {
            PartnerChannel.SMS -> partner.phone?.let {
                Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:" + Uri.encode(it))).putExtra("sms_body", text)
            }
            PartnerChannel.WHATSAPP -> installed(context, WHATSAPP, WHATSAPP_BUSINESS)?.let { pkg ->
                val digits = partner.phone.orEmpty().filter { it.isDigit() }
                Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/$digits?text=" + Uri.encode(text))).setPackage(pkg)
            }
            PartnerChannel.SIGNAL -> installed(context, SIGNAL)?.let { pkg -> plainText(text).setPackage(pkg) }
            PartnerChannel.OTHER -> null
        }
        try {
            context.startActivity(intent ?: Intent.createChooser(plainText(text), null))
        } catch (_: ActivityNotFoundException) {
            context.startActivity(Intent.createChooser(plainText(text), null))
        }
    }

    actual fun shareAny(context: PlatformContext, text: String) {
        context.startActivity(Intent.createChooser(plainText(text), null))
    }

    private fun plainText(text: String) = Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT, text)

    private fun installed(context: PlatformContext, vararg packages: String): String? = packages.firstOrNull {
        try {
            context.packageManager.getPackageInfo(it, 0)
            true
        } catch (_: PackageManager.NameNotFoundException) {
            false
        }
    }
}
