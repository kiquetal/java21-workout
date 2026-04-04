package dev.learning.service;


import dev.learning.repository.BookRepository;
import dev.learning.repository.LendingRepository;
import dev.learning.repository.MemberRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class LendingService {

    @Inject
    BookRepository bookRepository;

    @Inject
    MemberRepository memberRepository;

    @Inject
    LendingRepository lendingRepository;



}
