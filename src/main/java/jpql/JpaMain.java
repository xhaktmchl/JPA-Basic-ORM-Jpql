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
            System.out.println("====1");
            Member m1 = new Member();
            m1.setUsername("member1");
            m1.setAge(20);
            em.persist(m1);
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


