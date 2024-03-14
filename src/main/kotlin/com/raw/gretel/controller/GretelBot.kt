package com.raw.gretel.controller

import com.raw.gretel.service.EncryptionService
import com.raw.gretel.service.ResponseHandler
import com.raw.gretel.service.UserService
import io.micrometer.core.annotation.Counted
import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.MeterRegistry
import org.springframework.core.env.Environment
import org.springframework.stereotype.Component
import org.telegram.abilitybots.api.bot.AbilityBot
import org.telegram.abilitybots.api.objects.Ability
import org.telegram.abilitybots.api.objects.Flag
import org.telegram.abilitybots.api.objects.Locality
import org.telegram.abilitybots.api.objects.Privacy

@Component
class GretelBot(
    env: Environment,
    userService: UserService,
    private val meterRegistry: MeterRegistry
) : AbilityBot(env.getProperty("bot.token"), env.getProperty("bot.name")) {
    private val responseHandler: ResponseHandler = ResponseHandler(silent, db, userService, meterRegistry)


    override fun creatorId(): Long {
        return 1L
    }

    fun startBot(): Ability {
        return Ability.builder()
            .name("start")
            .info("Authenticate in bot")
            .locality(Locality.USER)
            .privacy(Privacy.PUBLIC)
            .action { ctx -> responseHandler.replyToStart(ctx) }
            .build()
    }

    @Counted
    fun checkInInBot(): Ability {
        return Ability.builder()
            .name("checkin")
            .info("Add chat to bot")
            .locality(Locality.GROUP)
            .privacy(Privacy.PUBLIC)
            .action { ctx -> responseHandler.checkIn(ctx) }
            .build()
    }

    @Counted
    fun sos(): Ability {
        return Ability.builder()
            .name("sos")
            .info("Delete user from all chats followed by bot. Initiate by user")
            .locality(Locality.ALL)
            .privacy(Privacy.PUBLIC)
            .action { ctx ->
                responseHandler.sos(ctx.user().id)
            }
            .build()
    }

    @Counted
    fun hide(): Ability {
        return Ability.builder()
            .name("hide")
            .info("Delete user from all chats followed by bot. Initiated by chat admin")
            .locality(Locality.GROUP)
            .privacy(Privacy.GROUP_ADMIN)
            .action { ctx ->
                responseHandler.hideByDemand(ctx.arguments()[0])
            }
            .build()
    }

    @Counted
    fun invite(): Ability {
        return Ability.builder()
            .name("invite")
            .info("Send invitation link to user")
            .locality(Locality.GROUP)
            .privacy(Privacy.GROUP_ADMIN)
            .action { ctx ->
                responseHandler.invite(ctx.arguments()[0], ctx.chatId())
            }
            .build()
    }

    fun approveJoinRequest(): Ability {
        return Ability.builder()
            .name(DEFAULT)
            .flag(Flag.CHAT_JOIN_REQUEST)
            .privacy(Privacy.PUBLIC)
            .locality(Locality.ALL)
            .action { ctx -> responseHandler.reviewJoinRequest(ctx.chatId(), ctx.user().id) }
            .build()
    }

    @Counted
    fun forgetMe(): Ability {
        return Ability.builder()
            .name("forget")
            .info("Delete user from bot")
            .locality(Locality.USER)
            .privacy(Privacy.PUBLIC)
            .action { ctx -> responseHandler.forgetMe(ctx.user().id) }
            .build()
    }

}