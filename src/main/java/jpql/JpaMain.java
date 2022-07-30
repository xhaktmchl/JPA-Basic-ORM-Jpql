package jpql;

import javax.persistence.*;
import java.util.List;

public class JpaMain {

    public static void main(String[] args) {
        // 엔티티매니저 팩토리 생성
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("hellojpa");
        // 엔티티매니저 팩토리 -> 엔티티 매니저 생성
        EntityManager em = emf.createEntityManager();

        EntityTransaction tx = em.getTransaction(); // JPA에서는 트랜섹션이 시작되고 해야 에러가 안나고 인식이 잘 된다.
        tx.begin();

        try{
            Team team = new Team();
            team.setName("team1");
            em.persist(team);

            Team team2 = new Team();
            team2.setName("team2");
            em.persist(team2);

            System.out.println("====1");
            Member m1 = new Member();
            m1.setUsername("member1");
            m1.setAge(20);
            m1.setTeam(team);
            m1.setType(MemberType.ADMIN);
            em.persist(m1);

            Member m2 = new Member();
            m2.setUsername("member2");
            m2.setAge(20);
            m2.setTeam(team);
            m2.setType(MemberType.ADMIN);
            em.persist(m2);

            Member m3 = new Member();
            m3.setUsername("member3");
            m3.setAge(20);
            m3.setTeam(team2);
            m3.setType(MemberType.ADMIN);
            em.persist(m3);

            Product product1 = new Product();
            product1.setStockAmount(5);
            product1.setName("product1");
            product1.setPrice(1000);
            em.persist(product1);

            System.out.println("====1");
            em.flush();
            em.clear();
            System.out.println("====1");
            /*JPQL*/
            /*
             TypeQuery, Query;
            * */
            System.out.println("====쿼리형");
            TypedQuery<Member> query1 = em.createQuery("select m from Member as m", Member.class); // 반환타입 명확할 때 사용
            Query query2 = em.createQuery("select m.username, m.age from Member as m");  // 반환 타입 잘 모를 때 사용

            // 조회
            query1.getResultList(); // 히스트 조회
            //query1.getSingleResult(); // 갯수가 1개가 아니면 에러나기 때문에 지양


            // 파라미터 바인딩
            System.out.println("====바인딩1");
            Member result1 =  em.createQuery("select m from Member as m where m.username = :username", Member.class)
                    .setParameter("username", "member1")
                    .getSingleResult();
            System.out.println(result1.getUsername());
            System.out.println("====바인딩2");
            List<Member> result2 =  em.createQuery("select m from Member as m where m.username = :username", Member.class)
                    .setParameter("username", "member1")
                    .getResultList();
            Member findM1 = result2.get(0);
            findM1.setAge(21);

            /* JPQL 프로젝션*/
            System.out.println("====프로젝션");
            // 조인 : sql 처럼 조인을 명시하는 것을 추천
            List<Team> result3= em.createQuery("select t from Member as m join m.team as t", Team.class)
                    .getResultList();

            // 스칼라 타입 프로젝션, 중복제거
            System.out.println("====스칼라 프로젝션");
            em.createQuery("select distinct m.username, m.age from Member m")
                    .getResultList();


            /*여러 값 프로젝션*/
            System.out.println("====여러값 프로젝션");
            //2.Object[]타입으로 조회
            System.out.println("====여러값 프로젝션2");
            List<Object[]> result4 = em.createQuery("select m.username, m.age from Member as m")
                    .getResultList();
            Object[] result5 = result4.get(0);
            System.out.println("username=" + result5[0]);
            System.out.println("age=" + result5[1]);

            //3.new 명령어로 조회
            System.out.println("====여러값 프로젝션3");
            List<MemberDTO> result6 = em.createQuery("select new jpql.MemberDTO(m.username, m.age) from Member as m", MemberDTO.class)
                    .getResultList();
            MemberDTO findMDto1 = result6.get(0);
            System.out.println("username=" + findMDto1.getName());
            System.out.println("age=" + findMDto1.getAge());


            /*
            페이징
             */
            List<Member> result7 = em.createQuery("select m from Member as m order by m.age desc",Member.class)
                    .setFirstResult(0)
                    .setMaxResults(10)
                    .getResultList();
            for(Member m: result7){
                System.out.println("member = " + m.getUsername());
            }


            /*
            조인
            * */
            //1.내부조인
            System.out.println("====1.내부조인");
            List<Member> result8 = em.createQuery("select m from Member as m inner join m.team as t where t.name = :teamName",Member.class)
                    .setParameter("teamName", "team1")
                    .getResultList();
            System.out.println(result8.get(0).getTeam());

            //2.외부조인
            //outer 생략가능
            List<Member> result9 = em.createQuery("select m from Member as m left join m.team t where t.name = :teamName",Member.class)
                    .setParameter("teamName", "team1")
                    .getResultList();
            System.out.println(result9.get(0).getTeam());

            //3.세타 조인

            System.out.println("====3.세타 조인");
            List<Member> result10 = em.createQuery("select m from Member m, Team t where m.username = t.name",Member.class)
                    .getResultList();
            //System.out.println(result10.get(0).getTeam()); null값 이면 에러남


            /*
            조인 필터링
             */
            //1.조인 대상 필터링
            System.out.println("====1.조인 대상 필터링");
            List<Member> result11 = em.createQuery("select m from Member m left join m.team t on t.name = 'team1'",Member.class)
                    .getResultList();
            System.out.println(result11.get(0).getTeam());

            //2.연관관계 없는 엔티티 외부조인
            // Team 외부 엔티티 자체를 조인함
            System.out.println("====2.연관관계 없는 엔티티 외부조인");
            List<Member> result12 = em.createQuery("select m from Member m left join Team t on m.username = t.name",Member.class)
                    .getResultList();
            System.out.println(result12.get(0).getTeam());

            /*
            서브쿼리
             */
            System.out.println("====서브쿼리1");
            List<Member> result13 = em.createQuery("select m from Member m where m.age > (select avg(m2.age) from Member m2)",Member.class)
                    .getResultList();

            System.out.println("====서브쿼리2");
            //TODO: 에러 해결
//            List<Member> result14 = em.createQuery("select m from Member m where (select count(o) from Order o where m=o.member)>0",Member.class)
//                    .getResultList();


            /*
            JPQL타입 표현
             */
            System.out.println("====JPQL타입 표현");
            List<Object[]> result14 = em.createQuery("select m.username, 'HELLO', true from Member m " +
                    "where m.type = jpql.MemberType.ADMIN and m.username is not null and m.age between 0 and 100")
                    .getResultList();
            for(Object[] o : result14){
                System.out.println("object: "+o[0]);
                System.out.println("object: "+o[1]);
                System.out.println("object: "+o[2]);
            }

            /*
            JPQL case조건식
             */
            System.out.println("====JPQL case조건식");
            List<String> result15 = em.createQuery("select " +
                    "case when m.age <= 10 then '학생요금' " +
                    "when m.age >50 then '노인요금' " +
                    "else '보통요금' " +
                    "end " +
                    "from Member m", String.class)
                    .getResultList();
            for(String str: result15){
                System.out.println("요금: "+str);
            }


            /*
            jpql함수
             */
            System.out.println("====jpql함수");
            List<String> result16 = em.createQuery("select concat('a','b') from Member m",String.class)
                    .getResultList();
            for(String str: result16){
                System.out.println("=="+str);
            }

            /*
            페치조인
             */
            // 기존 조인쿼리: 지연로딩처럼 연관 엔티티는 프록시 객체로 불러오고 메소드 요청시마다 쿼리를 날렸음.
            // 페치조인 한방쿼리: 페치조인을 하면 즉시로딩처럼 쿼리가 나갈때 관련 실제 엔티티까지 다 불러옴
            System.out.println("====페치조인");
            List<Member> result17 = em.createQuery("select m from Member m join fetch m.team",Member.class)
                    .getResultList();
            for(Member m: result17){
                System.out.println("=="+m.getUsername()+" "+m.getTeam().getName());
            }

            /*
            일대다 페치조인: 데이터 갯수가 다에 맞게 뻥튀기된다.
             */
            System.out.println("====일대다 페치조인");
            List<Team> result18 = em.createQuery("select t from Team t join fetch t.members", Team.class)
                    .getResultList();
            for(Team t: result18){
                System.out.println("=="+t.getName());
                for(Member m: t.getMembers()){
                    System.out.println("==member:"+m.getUsername());
                }
            }


            /*
            엔티티 직접 조회
             */
            // 엔티티를 직접 조회하더라도 PK 로 쿼리가 날라간다.
            System.out.println("====엔티티 직접 조회");
            List<Member> result19 = em.createQuery("select m from Member m where m = :member")
                    .setParameter("member", m1)
                    .getResultList();
            for(Member m: result19){
                System.out.println("=="+m.getUsername());
            }

            //ㅇ
            System.out.println("====엔티티 직접 조회2");
            List<Member> result20 = em.createQuery("select m from Member m where m.team = :team",Member.class)
                    .setParameter("team", team)
                    .getResultList();
            for(Member m: result20){
                System.out.println("=="+m.getUsername());
            }


            /*
            Named 쿼리 : 쿼리를 미리 정의해 놓고 호출해서 쓸 수 있다
             */
            System.out.println("====Named 쿼리");
            List<Member> result21 = em.createNamedQuery("Member.findByUsername",Member.class)
                    .setParameter("username", m1.getUsername())
                    .getResultList();
            for(Member m: result21){
                System.out.println("=="+m.getUsername());
            }


            /*
            벌크연산: 한방쿼리처럼 다수의 데이터를 업데이트,삭제 하는 쿼리
             */
            System.out.println("====벌크연산: 한방쿼리처럼 다수의 데이터를 업데이트,삭제 하는 쿼리");
            int result22 = em.createQuery("update Product p set p.price = p.price * 2 where p.stockAmount < :stockAmount")
                    .setParameter("stockAmount",10)
                    .executeUpdate();
            System.out.println("=="+result22);






            // 쓰기지연 sql 저장소에 모든 sql 실행
            tx.commit(); // 트랜섹션 요청 실행
        }catch (Exception e){
            tx.rollback();
        }finally {
            em.close();// 엔티티매니저 종료
        }
        emf.close();
    }
}


