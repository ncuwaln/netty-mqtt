package cn.edu.ncu;

import cn.edu.ncu.domain.TopicSub;
import org.junit.Test;

import java.util.LinkedHashSet;
import java.util.Set;

public class SetTests {

    class Entity{
        private int i;
        private String s;

        public int getI() {
            return i;
        }

        public void setI(int i) {
            this.i = i;
        }

        public String getS() {
            return s;
        }

        public void setS(String s) {
            this.s = s;
        }

        @Override
        public int hashCode() {
            int result = i;
            result = 31 * result + (s != null ? s.hashCode() : 0);
            return result;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Entity entity = (Entity) o;

            if (i != entity.i) return false;
            return s != null ? s.equals(entity.s) : entity.s == null;
        }
    }

    @Test
    public void hello(){
        Set<Entity> entities = new LinkedHashSet<Entity>();
        Entity entity = new Entity();
        entity.setI(1);
        entity.setS("2");
        Entity entity1 = new Entity();
        entity1.setI(1);
        entity1.setS("2");
        entities.add(entity);
        entities.add(entity1);
        assert entities.size() == 1;
    }
}
