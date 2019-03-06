package com.uniovi.controllers;

import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.uniovi.entities.Conversation;
import com.uniovi.entities.Message;
import com.uniovi.entities.User;
import com.uniovi.services.BidsService;
import com.uniovi.services.ConversationService;
import com.uniovi.services.MessageService;
import com.uniovi.services.UsersService;

@Controller
public class ConversationController {

	@Autowired
	private UsersService usersService;

	@Autowired
	private ConversationService conversationService;

	@Autowired
	private BidsService bidService;

	@Autowired
	private MessageService messageService;

	@RequestMapping(value = "/conversation", method = RequestMethod.GET)
	public String getConversations(Model model, HttpServletRequest request) {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		String email = auth.getName();
		User activeUser = usersService.getUserByEmail(email);

		List<Conversation> conversations = conversationService.getConversationUser(activeUser);

		model.addAttribute("conversationList", conversations);

		request.getSession().setAttribute("money", activeUser.getMoney());
		request.getSession().setAttribute("email", activeUser.getEmail());

		return "conversation/list";
	}

	@RequestMapping(value = "/conversation", method = RequestMethod.POST)
	public String getConversationsPost(HttpServletRequest request,
			@RequestParam(value = "bid_id", required = false) String bid_id,
			@RequestParam(value = "conversation_id", required = false) String conversation_id) {
		// Si ya existe la conversacion que vaya directamente a la conversacion
		if (conversation_id != null && conversation_id.length() > 0)
			return "redirect:/conversation/" + conversation_id;
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		String email = auth.getName();
		User activeUser = usersService.getUserByEmail(email);

		Long conversationID;
		// Buscamos la conversacion por si existe, si no existe crearemos una nueva.
		Conversation c = conversationService
				.getConversationByBidAndInterested(bidService.getBid(Long.parseLong(bid_id)), activeUser);
		if (c == null) { // Como no existe la conversacion creamos una nueva.
			c = new Conversation(activeUser, bidService.getBid(Long.parseLong(bid_id))); // La creamos
			conversationService.addConversation(c); // Y la añadimos
		}
		conversationID = c.getId();

		return "redirect:/conversation/" + conversationID;
	}

	@RequestMapping(value = "/conversation/{id}", method = RequestMethod.GET)
	public String getConversationId(Model model, HttpServletRequest request, @PathVariable Long id) {
		Conversation c = conversationService.getConversation(id);
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		String email = auth.getName();
		User activeUser = usersService.getUserByEmail(email);
		// Chequeo, comprobar si es suya la conversacion, si no sera expulsado a la
		// vista de conversaciones.
		// Comprobamos si es el que envia o el que lo vende.
		if (c.getBid().getUser().getId() != activeUser.getId() && c.getInterestedUser().getId() != activeUser.getId())
			return "redirect:/conversation";

		// Mostrar mensajes actuales
		List<Message> messages = messageService.getMessagesFromConversation(c);

		model.addAttribute("bidName", c.getBid().getTitle());
		model.addAttribute("messageList", messages);
		model.addAttribute("conversationId", c.getId());

		return "conversation/chat";
	}

	@RequestMapping(value = "/conversation/{id}", method = RequestMethod.POST)
	public String getConversationIdPost(Model model, HttpServletRequest request,
			@RequestParam("message") String message, @PathVariable Long id) {
		Conversation c = conversationService.getConversation(id);
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		String email = auth.getName();
		User activeUser = usersService.getUserByEmail(email);
		// Chequeo, comprobar si es suya la conversacion, si no sera expulsado a la
		// vista de conversaciones.
		// Comprobamos si es el que envia o el que lo vende.
		if (c.getBid().getUser().getId() != activeUser.getId() && c.getInterestedUser().getId() != activeUser.getId())
			return "redirect:/conversation";

		// Añadir el nuevo mensaje
		Message newMessage = new Message(c, activeUser, new Date(), message);
		messageService.addMessage(newMessage);

		// Mostrar los nuevos mensajes
		List<Message> messages = messageService.getMessagesFromConversation(c);

		model.addAttribute("bidName", c.getBid().getTitle());
		model.addAttribute("messageList", messages);
		model.addAttribute("conversationId", c.getId());

		return "conversation/chat";
	}
	
	@RequestMapping(value = "/conversation/delete", method = RequestMethod.POST)
	public String getConversationIdPost(Model model, HttpServletRequest request,
			@RequestParam("conversation_id") Long conversation_id) {
		Conversation c = conversationService.getConversation(conversation_id);
		if(c == null) //Si no existe no hay nada que borrar
			return "redirect:/conversation/";
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		String email = auth.getName();
		User activeUser = usersService.getUserByEmail(email);
		// Chequeo, comprobar si es suya la conversacion, si no sera expulsado a la
		// vista de conversaciones.
		// Comprobamos si es el que envia o el que lo vende.
		if (c.getBid().getUser().getId() != activeUser.getId() && c.getInterestedUser().getId() != activeUser.getId())
			return "redirect:/conversation/";

		// Borrar el chat
		conversationService.deleteConversation(conversation_id);

		return "redirect:/conversation";
	}
}
