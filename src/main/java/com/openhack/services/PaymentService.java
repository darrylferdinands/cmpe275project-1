package com.openhack.services;

import com.openhack.dao.HackathonDao;
import com.openhack.dao.PaymentDao;
import com.openhack.dao.TeamMemberDao;
import com.openhack.dao.UserDao;
import com.openhack.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.transaction.Transactional;
import java.util.Optional;

@Service
public class PaymentService {
    @Autowired
    private PaymentDao paymentDao;

    @Autowired
    private UserDao userDao;

    @Autowired
    private HackathonDao hackathonDao;

    @Autowired
    private TeamMemberDao teamMemberDao;

    @Transactional
    public ResponseEntity<?> createPayment(Long uid,Long hid){
        User user = userDao.findById(Optional.ofNullable(uid).orElse(-1L));
        Hackathon hackathon = hackathonDao.findItemById(Optional.ofNullable(hid).orElse(-1L));

        TeamMember teamMember = teamMemberDao.findItemByUid((int)user.getUid());

        if(teamMember.getP_status().equals("Paid"))
            return ResponseEntity.badRequest().body("Payment already received for this hackathon");
        teamMember.setP_status("Paid");

        Payment payment = new Payment();
        payment.setHackathon(hackathon);
        payment.setUid(user.getUid());

        paymentDao.createItem(payment);

        return ResponseEntity.ok().body("Payment Successfull");

    }

    @Transactional
    public ResponseEntity<?> getPaymentAmount(Long uid,Long hid){
        User user = userDao.findById(Optional.ofNullable(uid).orElse(-1L));
        Hackathon hackathon = hackathonDao.findItemById(Optional.ofNullable(hid).orElse(-1L));

        float fee = hackathon.getFee();
        int discount = hackathon.getDiscount();
        float amount = fee;
        if(user != null)
            if(user.getOrganization().getName().equals(hackathon.getSponser().getName()) && user.getOrgStatus().equals("Approved")){
                amount = (fee * discount)/100;
            }

        return ResponseEntity.ok().body(Float.toString(amount));
    }

}
