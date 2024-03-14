package com.raw.gretel.service

import com.raw.gretel.domain.*
import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.MeterRegistry
import org.telegram.abilitybots.api.db.DBContext
import org.telegram.abilitybots.api.objects.MessageContext
import org.telegram.abilitybots.api.sender.SilentSender
import org.telegram.telegrambots.meta.api.methods.groupadministration.*
import org.telegram.telegrambots.meta.api.methods.send.SendMessage


class ResponseHandler(
    val sender: SilentSender,
    val db: DBContext,
    val userService: UserService,
    private val meterRegistry: MeterRegistry

) {
    private val chatStates: Map<Long, UserState> = db.getMap(CHAT_STATES)

    fun replyToStart(ctx: MessageContext) {
        val message = SendMessage()
        message.setChatId(ctx.chatId())
        message.text = START_TEXT
        userService.saveUser(ctx.user().id, "@${ctx.user().userName}", ctx.chatId())
        sender.execute(message)
        val counter = Counter.builder("bot_started_total")
            .description("A number of bot starts")
            .register(meterRegistry)
        counter.increment()
    }

    fun checkIn(ctx: MessageContext) {
        if (ctx.user().isBot) {
            val message = SendMessage()
            message.chatId = ctx.chatId().toString()
            message.text = IS_BOT
            sender.execute(message)
        }
        userService.addGroupToUser(ctx.user().id, ctx.user().userName, ctx.chatId())
        val counter = Counter.builder("check_in_executed_total")
            .tag("version", "v1")
            .description("A number of checkin command executions")
            .register(meterRegistry)
        counter.increment()
    }

    fun sos(userId: Long) {
        val user = userService.getUserById(userId)
        user?.username?.let { userService.changeStatusUser(it, UserState.HIDE) }
        val ban = BanChatMember()
        ban.userId = userId
        val unbanChatMember = UnbanChatMember()
        unbanChatMember.userId = userId
        user?.groups?.forEach {
            ban.chatId = it
            unbanChatMember.chatId = it
            sender.execute(ban)
            sender.execute(unbanChatMember)
        }
        user?.groups?.forEach {
            val message = SendMessage()
            message.chatId = it
            message.text = "$HIDDEN_USER ${user.username}"
            sender.execute(message)
        }
        val counter = Counter.builder("sos_executed_total")
            .tag("version", "v1")
            .description("A number of sos command executions")
            .register(meterRegistry)
        counter.increment()
    }

    fun hideByDemand(username: String) {
        val user = userService.getUserByUsername(username)
        user?.userId?.let { sos(it.toLong()) }
        user?.groups?.forEach {
            val message = SendMessage()
            message.chatId = it
            message.text = "$HIDDEN_USER ${user.username}"
            sender.execute(message)
        }

        val counter = Counter.builder("hide_executed_total")
            .tag("version", "v1")
            .description("A number of hide command executions")
            .register(meterRegistry)
        counter.increment()
    }

    fun invite(username: String, chatId: Long) {
        val user = userService.getUser(username, chatId)

        if (user == null) {
            sender.send(USER_NOT_IN_A_GROUP, chatId)
            return
        }

        val inviteLink = CreateChatInviteLink.builder()
            .chatId(chatId)
            .expireDate((System.currentTimeMillis() / 1000 + 3_600).toInt())
            .createsJoinRequest(true)
            .name("Return back")
            .build()

        sender.execute(inviteLink).ifPresent { link ->
            val message = SendMessage().apply {
                text = "$RETURN_BACK ${link.inviteLink}"
                this.chatId = user.chatId.toString()
            }
            sender.execute(message)

            val counter = Counter.builder("invite_executed_total")
                .tag("version", "v1")
                .description("A number of invite command executions")
                .register(meterRegistry)
            counter.increment()
        }
    }

    fun reviewJoinRequest(groupId: Long, userId: Long) {
        val user = userService.getUserById(userId)
        if (user?.groups?.contains(groupId.toString()) == true) {
            val approve = ApproveChatJoinRequest.builder()
                .chatId(groupId)
                .userId(userId)
                .build()
            sender.execute(approve)
            userService.changeStatusUser(user.username, UserState.ACTIVE)
            return
        }
        val decline = DeclineChatJoinRequest.builder()
            .chatId(groupId)
            .userId(userId)
            .build()
        sender.execute(decline)
    }

    fun forgetMe(userId: Long) {
        val chatId = userService.delete(userId)
        if (chatId == 0L) {
            return
        }
        val message = SendMessage()
        message.setChatId(chatId)
        message.text = FORGET_ME
        sender.execute(message)
    }

    fun userIsHidden(chatId: Long): Boolean {
        return true
    }

}