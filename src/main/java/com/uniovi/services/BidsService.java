package com.uniovi.services;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.uniovi.entities.Bid;
import com.uniovi.entities.User;
import com.uniovi.repositories.BidsRepository;

@Service
public class BidsService {
	
	@Autowired
	private HttpSession httpSession;

	@Autowired
	private BidsRepository bidsRepository;
	
	/**
	 * Devuelve las ofertas por usuario.
	 * Si eres ADMIN te las devolvera todas.
	 * @param pageable
	 * @param user
	 * @return
	 */
	public Page<Bid> getBidsForUser(Pageable pageable, User user) {
		Page<Bid> bids = new PageImpl<Bid>(new LinkedList<Bid>());
		if (user.getRole().equals("ROLE_USER")) {
			bids = bidsRepository.findAllByUser(pageable, user);
		}
		if (user.getRole().equals("ROLE_ADMIN")) {
			bids = getBids(pageable);
		}
		return bids;
	}
	
	/**
	 * Devuelve todas las ofertas disponibles.
	 * @param pageable
	 * @return
	 */
	public Page<Bid> getBids(Pageable pageable) {
		Page<Bid> bids = bidsRepository.findAll(pageable);
		return bids;
	}
	
	/**
	 * Devuelve una oferta por su id.
	 * @param id
	 * @return
	 */
	public Bid getBid(Long id) {
		@SuppressWarnings("unchecked")
		Set<Bid> consultedListBid = (Set<Bid>) httpSession.getAttribute("consultedListBid");
		if (consultedListBid == null) {
			consultedListBid = new HashSet<Bid>();
		}
		Bid bidObtained = bidsRepository.findById(id).get();
		consultedListBid.add(bidObtained);
		httpSession.setAttribute("consultedListBid", consultedListBid);
		return bidObtained;
	}
	
	/**
	 * Metodo para añadir una nueva oferta.
	 * @param bid
	 */
	public void addBid(Bid bid) {
		bidsRepository.save(bid);
	}

	/**
	 * Metodo para borrar la oferta.
	 * REALMENTE, no la borra, la pone en estado DELETED.
	 * @param id
	 */
	public void deleteBid(Long id) {
		bidsRepository.deleteById(id);
	}
	
}
