package net.workbook.mileage.interfaces;

public interface Sectionizer<T> 

{
    /**
     * Returns the title for the given instance from the data source.
     * 
     * @param instance The instance obtained from the data source of the decorated list adapter.
     * @return section title for the given instance.
     */
    String getSectionTitleForItem(T instance);
}
