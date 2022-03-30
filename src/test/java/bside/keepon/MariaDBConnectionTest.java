package bside.keepon;

import bside.keepon.user.entity.Member;
import bside.keepon.user.etc.SnsType;
import bside.keepon.user.repository.MemberRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@Transactional
public class MariaDBConnectionTest {
    @Autowired
    private MemberRepository memberRepository;
/*
    @Test
    public void dbConnection(){
        Member bySnsInfo = memberRepository.findBySnsInfo(SnsType.naver, "naver-test-sns-id");
        Assertions.assertThat(bySnsInfo.getId()).isEqualTo(1L);
    }
 */
}
