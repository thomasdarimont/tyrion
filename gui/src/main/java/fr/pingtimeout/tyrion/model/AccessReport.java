package fr.pingtimeout.tyrion.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class AccessReport {


    private final Map<Accessor, Set<Access>> criticalSectionsPerAccessor;
    private final Map<Target, Set<Access>> criticalSectionsPerTarget;


    public AccessReport(Set<Access> criticalSections) {
        criticalSectionsPerAccessor = new HashMap<>();
        criticalSectionsPerTarget = new HashMap<>();

        for (Access access : criticalSections) {
            addAccessForAccessor(access);
            addAccessForTarget(access);
        }
    }


    private void addAccessForAccessor(Access access) {
        Accessor accessor = access.getAccessor();

        if (!criticalSectionsPerAccessor.containsKey(accessor)) {
            criticalSectionsPerAccessor.put(accessor, new TreeSet<Access>());
        }

        Set<Access> criticalSectionsForAccessor = criticalSectionsPerAccessor.get(accessor);
        criticalSectionsForAccessor.add(access);
    }

    private void addAccessForTarget(Access access) {
        Target target = access.getTarget();

        if (!criticalSectionsPerTarget.containsKey(target)) {
            criticalSectionsPerTarget.put(target, new TreeSet<Access>());
        }

        Set<Access> criticalSectionsForTarget = criticalSectionsPerTarget.get(target);
        criticalSectionsForTarget.add(access);
    }


    public Set<Access> getCriticalSectionsForAccessor(Accessor accessor) {
        return criticalSectionsPerAccessor.get(accessor);
    }


    public Set<Access> getCriticalSectionsForLock(Target lock) {
        return criticalSectionsPerTarget.get(lock);
    }
}