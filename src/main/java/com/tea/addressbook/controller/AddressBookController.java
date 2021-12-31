package com.tea.addressbook.controller;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.supercsv.io.CsvBeanWriter;
import org.supercsv.io.ICsvBeanWriter;
import org.supercsv.prefs.CsvPreference;

import com.tea.addressbook.entity.AddressBook;
import com.tea.addressbook.service.AddressBookService;

@Controller
public class AddressBookController {

	@Value("${uploadDir}")
	private String uploadFolder;
	
	@Autowired
	private AddressBookService addressBookService;


	
	
	
	  @GetMapping("/all-contacts/export")
	    public void exportToCSV(HttpServletResponse response) throws IOException {
	        response.setContentType("text/csv");
	        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
	        String currentDateTime = dateFormatter.format(new Date());
	         
	        String headerKey = "Content-Disposition";
	        String headerValue = "attachment; filename=users_" + currentDateTime + ".csv";
	        response.setHeader(headerKey, headerValue);
	         
	        List<AddressBook> listContacts = addressBookService.getAllContacts();
	 
	        ICsvBeanWriter csvWriter = new CsvBeanWriter(response.getWriter(), CsvPreference.STANDARD_PREFERENCE);
	        String[] csvHeader = {"User ID","Name", "Address", "Created At"};
	        String[] nameMapping = {"id", "name", "address", "createDate"};
	         
	        csvWriter.writeHeader(csvHeader);
	         
	        for (AddressBook contact : listContacts) {
	            csvWriter.write(contact, nameMapping);
	        }
	         
	        csvWriter.close();
	         
	    }
	
	
	

	@GetMapping(value = {"/create"})
	public String createContactPage() {
		return "create";
	}
	
	@GetMapping(value = {"/", "/home"})
	public String indexPage() {
		return "index";
	}
	
	@GetMapping(value = {"/all-contacts"})
	public String showAll(Model map) {
		List<AddressBook> contacts = addressBookService.getAllContacts();
		map.addAttribute("contacts", contacts);
		return "table-contacts";
		
	}
	
	@GetMapping("/contact/display/{id}")
	@ResponseBody
	void showImage(@PathVariable("id") Long id, HttpServletResponse response, Optional<AddressBook> addressBook)
			throws ServletException, IOException {
		addressBook = addressBookService.getContactById(id);
		response.setContentType("image/jpeg, image/jpg, image/png, image/gif");
		response.getOutputStream().write(addressBook.get().getImage());
		response.getOutputStream().close();
	}
	
	
	@PostMapping("/contact/create")
	public @ResponseBody ResponseEntity<?> createContact(@RequestParam("name") String name,
			 @RequestParam("address") String address, Model model, HttpServletRequest request
			,final @RequestParam("image") MultipartFile file) {
	
		try {
			String uploadDirectory = request.getServletContext().getRealPath(uploadFolder);
			String fileName = file.getOriginalFilename();
			String filePath = Paths.get(uploadDirectory, fileName).toString();
			if (fileName == null || fileName.contains("..")) {
				model.addAttribute("invalid", "Sorry! Filename contains invalid path sequence \" + fileName");
				return new ResponseEntity<>("Sorry! Filename contains invalid path sequence " + fileName, HttpStatus.BAD_REQUEST);
			}
			String[] names = name.split(",");
			String[] addresses = address.split(",");
			Date createDate = new Date();
			try {
				File dir = new File(uploadDirectory);
				if (!dir.exists()) {
					dir.mkdirs();
				}
				// Save the file locally
				BufferedOutputStream stream = new BufferedOutputStream(new FileOutputStream(new File(filePath)));
				stream.write(file.getBytes());
				stream.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
			byte[] imageData = file.getBytes();
			AddressBook addressBook = new AddressBook();
			addressBook.setName(names[0]);
			addressBook.setImage(imageData);
			addressBook.setAddress(addresses[0]);
			addressBook.setCreateDate(createDate);
			addressBookService.saveContact(addressBook);
			return new ResponseEntity<>("Contact Saved With File - " + fileName, HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
	}
	
	

	
	@GetMapping(value = {"/all-contacts/details"})
	public String viewContactPage(@RequestParam("id") Long id, Optional<AddressBook> addressBook, Model model) {
		try {
			if (id != 0) {
				addressBook = addressBookService.getContactById(id);
				if (addressBook.isPresent()) {
					model.addAttribute("id", addressBook.get().getId());
					model.addAttribute("name", addressBook.get().getName());
					model.addAttribute("address", addressBook.get().getAddress());	
					model.addAttribute("address", addressBook.get().getCreateDate());	
					return "view";
				}
				return "view";
			}
		return "table-contacts";
		} catch (Exception e) {
			e.printStackTrace();
			return "table-contacts";
		}	
	
	}
	
	@GetMapping(value = {"/all-contacts/edit"})
	public String editContactPage(@RequestParam("id") Long id, Optional<AddressBook> addressBook, Model model) {
		try {
			if (id != 0) {
				addressBook = addressBookService.getContactById(id);
				if (addressBook.isPresent()) {
					model.addAttribute("id", addressBook.get().getId());
					model.addAttribute("name", addressBook.get().getName());
					model.addAttribute("address", addressBook.get().getAddress());			
					return "edit";
				}
				return "edit";
			}
		return "table-contacts";
		} catch (Exception e) {
			e.printStackTrace();
			return "table-contacts";
		}	
	
	}
	
	@PostMapping("/all-contacts/edit/{id}")
	public @ResponseBody ResponseEntity<?> editContact(@RequestParam("id") Long id,
			@RequestParam("name") String name,
			 @RequestParam("address") String address, Model model, HttpServletRequest request
			,final @RequestParam("image") MultipartFile file,
			Optional<AddressBook> addressBook) {
		String[] names = name.split(",");
		String[] addresses = address.split(",");
		try {
			//If no new image uploaded
			if(file.getSize() == 0) {
				//Get contact from database with this id
				addressBook = addressBookService.getContactById(id);
//				AddressBook addressBook = new AddressBook();
				addressBook.setName(names[0]);
				addressBook.setAddress(addresses[0]);
				addressBookService.updateContact(addressBook);
				return new ResponseEntity<>("Contact Updated Successfully", HttpStatus.OK);
				
			}else {
				//if new image is uploaded
				String uploadDirectory = request.getServletContext().getRealPath(uploadFolder);
				String fileName = file.getOriginalFilename();
				String filePath = Paths.get(uploadDirectory, fileName).toString();
				if (fileName == null || fileName.contains("..")) {
					model.addAttribute("invalid", "Sorry! Filename contains invalid path sequence \" + fileName");
					return new ResponseEntity<>("Sorry! Filename contains invalid path sequence " + fileName, HttpStatus.BAD_REQUEST);
				}
			
				try {
					File dir = new File(uploadDirectory);
					if (!dir.exists()) {
						dir.mkdirs();
					}
					// Save the file locally
					BufferedOutputStream stream = new BufferedOutputStream(new FileOutputStream(new File(filePath)));
					stream.write(file.getBytes());
					stream.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
				byte[] imageData = file.getBytes();
				 addressBook = addressBookService.getContactById(id);
			
//				AddressBook addressBook = new AddressBook();
				addressBook.setName(names[0]);
				addressBook.setImage(imageData);
				addressBook.setAddress(addresses[0]);
				addressBookService.updateContact(addressBook);
				return new ResponseEntity<>("Contact Updated Successfully", HttpStatus.OK);
			
				
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
	
	}
//	
	
	@RequestMapping("/all-contacts/delete/{id}")
	public String deleteContact(@PathVariable(name = "id") Long id) {
		try {
			addressBookService.deleteContact(id);
	
		} catch (Exception e) {
			e.printStackTrace();
			return "redirect:/all-contacts";
		}	
		return "redirect:/all-contacts";
	}

	
}
