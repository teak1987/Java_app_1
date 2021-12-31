package com.tea.addressbook.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tea.addressbook.entity.AddressBook;

@Repository
public interface AddressBookRepository extends JpaRepository<AddressBook, Long>{

}
