

create materialized view license_deficiency_view as
select fsv.owner_name,fsv.deficiency_code,fsv.inspectiondate,temp2.dba_name,temp2.license_number from
foodstoreviolations fsv 
inner join
(
select rfs.license_number,rfs.entity_name,rfs.dba_name,addr.street_num,addr.street_name,addr.zip_code,rfs.county from
RetailFoodStore rfs inner join address addr on rfs.license_number=addr.license_number
) temp2
on fsv.street_num=temp2.street_num and 
fsv.street_name = temp2.street_name;


create or replace function refresh_mat_view()
returns trigger language plpgsql
as $$
begin
    refresh materialized view license_deficiency_view;
    return null;
end $$;


create trigger refresh_mat_view
after insert or update or delete or truncate
on retailfoodstore for each statement 
execute procedure refresh_mat_view();

create trigger refresh_mat_view
after insert or update or delete or truncate
on address for each statement 
execute procedure refresh_mat_view();

create trigger refresh_mat_view
after insert or update or delete or truncate
on foodstoreviolations for each statement 
execute procedure refresh_mat_view();

