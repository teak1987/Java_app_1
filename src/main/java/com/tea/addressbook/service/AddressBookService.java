package com.tea.addressbook.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.tea.addressbook.entity.AddressBook;
import com.tea.addressbook.repository.AddressBookRepository;

@Service
public class AddressBookService {

	@Autowired
	private AddressBookRepository addressBookRepository;
	
	public void saveContact(AddressBook addressBook) {
		addressBookRepository.save(addressBook);
	}
	
	public List<AddressBook> getAllContacts(){
		return addressBookRepository.findAll(Sort.by("name").ascending());
	}
	
	public Optional<AddressBook> getContactById(Long id){
		return addressBookRepository.findById(id);
	}
	
	public void updateContact(AddressBook contact) {
		addressBookRepository.save(contact);
	}

	
	public void deleteContact(Long id) {
		addressBookRepository.deleteById(id);
	}
	

	
}
